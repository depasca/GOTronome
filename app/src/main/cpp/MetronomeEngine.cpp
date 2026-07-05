#include "MetronomeEngine.h"
#include <cmath>
#include <chrono>
#include <android/log.h>

const int PLAYING_STATE_PLAYING = 1;
const int PLAYING_STATE_STOPPED = 0;
const int PLAYING_STATE_SILENT = 2;
const int PLAYING_STATE_COUNT_IN = 3;

MetronomeEngine::MetronomeEngine() {
    isPlaying = false;
    currentBeat = 0;
    currentMeasure = 0;
    silentMeasureCounter = 0;
    isSilent = false;
    silentMeasures = 0;
}

MetronomeEngine::~MetronomeEngine() {
    stop();
}

oboe::Result MetronomeEngine::start(int _beatsPerMinute, int _beatsPerMeasure) {
    std::lock_guard<std::mutex> lock(mLock);
    LOGD("MetronomeEngine::start");
    this->beatsPerMinute = _beatsPerMinute;
    this->beatsPerMeasure = _beatsPerMeasure;

    // Reset the transport so the first beat fires immediately and in phase.
    currentBeat.store(0, std::memory_order_relaxed);
    currentMeasure = 0;
    silentMeasureCounter = 0;
    isSilent.store(false, std::memory_order_relaxed);
    beatPhase = 0.0;
    samplesSinceBeat = 0;

    // Arm a one-bar count-in lead-in before the song proper.
    isCountingIn = countInEnabled.load(std::memory_order_relaxed);
    countInBeats = beatsPerMeasure;
    countInBeat = 0;

    oboe::Result result = oboe::Result::OK;
    int tryCount = 0;
    do {
        if (tryCount > 0) {
            usleep(20 * 1000); // Sleep between tries to give the system time to settle.
        }
        result = createStream();
        if (result == oboe::Result::OK) {
            LOGD("MetronomeEngine::start stream created!");
            // Base timing on the rate the stream was actually granted, not the request.
            sampleRate = stream->getSampleRate();
            samplesPerBeat = (sampleRate * 60.0) / beatsPerMinute;
            result = stream->start();
            if (result != oboe::Result::OK) {
                LOGW("Error starting playback stream. Error: %s, attempt num %d",
                     oboe::convertToText(result), tryCount);
                stream->close();
                stream.reset();
            }
            else {
                LOGD("MetronomeEngine::start stream started!");
            }
        }
        else{
            LOGW("Error creating playback stream. Error: %s, attempt num %d",
                 oboe::convertToText(result), tryCount);
        }

    } while (result != oboe::Result::OK && tryCount++ < 3);
    if (result != oboe::Result::OK) {
        LOGE("Error creating playback stream. Error: %s",
             oboe::convertToText(result));
        isPlaying = false;
    }
    else {
        isPlaying = true;
    }
    return result;
}

oboe::Result  MetronomeEngine::stop() {
    LOGD("MetronomeEngine::stop");
    oboe::Result result = oboe::Result::OK;
    // Stop, close and delete in case not already closed.
    std::lock_guard<std::mutex> lock(mLock);
    int tryCount = 0;
    do {
        if (tryCount > 0) {
            usleep(20 * 1000); // Sleep between tries to give the system time to settle.
        }
        if (stream) {
            result = stream->stop();
            if (result != oboe::Result::OK) {
                LOGW("Error stopping playback stream. Error: %s",
                     oboe::convertToText(result));
            } else {
                stream->close();
                stream.reset();
                isPlaying = false;
                isSilent = false;
                currentBeat = 0;
                currentMeasure = 0;
                silentMeasureCounter = 0;
                isCountingIn = false;
                countInBeat = 0;
                beatPhase = 0.0;
                samplesSinceBeat = 0;
            }
        }
    } while (result != oboe::Result::OK && tryCount++ < 3);
    if (result != oboe::Result::OK) {
        LOGE("Error stopping playback stream. Error: %s",
             oboe::convertToText(result));
        isPlaying = true;
    }
    return result;
}

oboe::Result MetronomeEngine::createStream() {
    oboe::AudioStreamBuilder builder;
    oboe::Result result = builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setChannelCount(1)
            ->setSampleRate(static_cast<int>(sampleRate))
            ->setCallback(this)
            ->setDirection(oboe::Direction::Output)
            ->openStream(stream);
    LOGD("MetronomeEngine::createStream result -> %s", oboe::convertToText(result));
    return result;
}

float envelope(float t, float duration) {
    float attack = 0.002f;
    float release = 0.008f;
    if (t < attack) return t / attack;
    else if (t > duration - release) return (duration - t) / release;
    else return 0.9f;
}

void MetronomeEngine::generateTick(float *buffer, int32_t numFrames) {
    const float tickVolume = 0.3f;
    const float accentVolume = 0.5f;
    const int tickLength = static_cast<int>(sampleRate * 0.01); // 10ms tick
    const double period = samplesPerBeat;
    const bool silentEnabled = silentMeasureEnabled.load(std::memory_order_relaxed);
    const int numSilent = silentMeasures.load(std::memory_order_relaxed);

    for (int i = 0; i < numFrames; ++i) {
        // Fire a beat whenever a full (fractional) beat period has elapsed. The
        // remainder carries into beatPhase, so timing never drifts.
        if (beatPhase <= 0.0) {
            beatPhase += period;
            samplesSinceBeat = 0;

            if (isCountingIn.load(std::memory_order_relaxed)) {
                if (countInBeat >= countInBeats) {
                    // Lead-in complete: begin the song on this beat as a fresh
                    // downbeat so measure/silent/bar logic starts aligned.
                    isCountingIn.store(false, std::memory_order_relaxed);
                    currentBeat.store(1, std::memory_order_relaxed);
                    currentMeasure = 0;
                    isSilent.store(false, std::memory_order_relaxed);
                    silentMeasureCounter = 0;
                } else {
                    // Count 1..N without advancing the song.
                    currentBeat.store(++countInBeat, std::memory_order_relaxed);
                }
            } else {
                int beat = currentBeat.load(std::memory_order_relaxed) + 1;
                if (beat > beatsPerMeasure) {
                    beat = 1;
                    currentMeasure++;
                    if (silentEnabled) {
                        if (isSilent.load(std::memory_order_relaxed)) {
                            if (++silentMeasureCounter >= numSilent) {
                                isSilent.store(false, std::memory_order_relaxed);
                                silentMeasureCounter = 0;
                            }
                        } else if (numSilent > 0) {
                            isSilent.store(true, std::memory_order_relaxed);
                        }
                    }
                }
                currentBeat.store(beat, std::memory_order_relaxed);
            }
        }

        const int beat = currentBeat.load(std::memory_order_relaxed);
        const bool isTick = samplesSinceBeat < tickLength;
        const float freq = (beat == 1) ? 1760.0f : 880.0f;
        float volume = (beat == 1) ? accentVolume : tickVolume;
        if (isSilent.load(std::memory_order_relaxed)) {
            volume = 0.0f;
        }
        if (isTick) {
            const float t = static_cast<float>(samplesSinceBeat) / sampleRate;
            const float env = envelope(t, tickLength / sampleRate);
            buffer[i] = volume * env * sinf(2.0f * M_PI * freq * t);
        } else {
            buffer[i] = 0.0f;
        }

        beatPhase -= 1.0;
        samplesSinceBeat++;
    }
}

double MetronomeEngine::getCurrentTimeSeconds() {
    return 0; //static_cast<double>(frameCounter) / sampleRate;
}

int MetronomeEngine::getCurrentBeat() const {
    return currentBeat;
}

int MetronomeEngine::getPlayingState() {
    if(isPlaying){
        if (isCountingIn.load(std::memory_order_relaxed)) return PLAYING_STATE_COUNT_IN;
        return isSilent ? PLAYING_STATE_SILENT : PLAYING_STATE_PLAYING;
    }
    return PLAYING_STATE_STOPPED;
}

oboe::DataCallbackResult MetronomeEngine::onAudioReady(oboe::AudioStream *_stream,
                                                       void *audioData,
                                                       int32_t numFrames) {
    auto *floatData = static_cast<float *>(audioData);
    generateTick(floatData, numFrames);
    return oboe::DataCallbackResult::Continue;
}

void MetronomeEngine::setJavaVM(JavaVM *vm, jobject callbackObject) {
    javaVm = vm;
    JNIEnv *env;
    vm->AttachCurrentThread(&env, nullptr);

    javaCallbackObj = env->NewGlobalRef(callbackObject);

    jclass cls = env->FindClass("com/pdp/gotronome/Metronome");
    onBeatMethod = env->GetStaticMethodID(cls, "onNativeBeat", "(I)V");
}

// not using this for now, because kotlin pulls the info at every frame
void MetronomeEngine::sendBeatToJava(int beat) {
    if (javaVm && onBeatMethod) {
        JNIEnv *env;
        javaVm->AttachCurrentThread(&env, nullptr);
        jclass c = env->GetObjectClass(javaCallbackObj);
        if (c == nullptr) {
            LOGE("GOT-MetronomeEngine Failed to find class Metronome");
        }
        else{
            env->CallStaticVoidMethod(c, onBeatMethod, beat);
        }
    }
}

void MetronomeEngine::setNumSilentMeasures(int val) {
    silentMeasures = val;
    LOGD("MetronomeEngine::setNumSilentMeasures -> %d, isSilent -> %d",
         silentMeasures.load(), isSilent.load());
}

void MetronomeEngine::setSilentMeasuresEnabled(bool b) {
    silentMeasureEnabled = b;
}

void MetronomeEngine::setCountInEnabled(bool b) {
    countInEnabled = b;
}

