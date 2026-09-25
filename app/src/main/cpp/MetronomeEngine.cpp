#include "MetronomeEngine.h"
#include <algorithm>
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
    // Default: accent beat 1, every other beat normal.
    for (int i = 0; i < MAX_BEATS; ++i) {
        accentPattern[i].store(i == 0 ? 2 : 1, std::memory_order_relaxed);
    }
    for (auto &step : grooveStepVoices) {
        step.store(0, std::memory_order_relaxed);
    }
    resetVoices();
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
    resetVoices();

    // Arm a one-bar count-in lead-in before the song proper.
    isCountingIn = countInEnabled.load(std::memory_order_relaxed);
    countInBeats = beatsPerMeasure;
    countInBeat = 0;

    oboe::Result result = startStream();
    isPlaying = (result == oboe::Result::OK);
    return result;
}

oboe::Result MetronomeEngine::startStream() {
    oboe::Result result = oboe::Result::OK;
    int tryCount = 0;
    do {
        if (tryCount > 0) {
            usleep(20 * 1000); // Sleep between tries to give the system time to settle.
        }
        result = createStream();
        if (result == oboe::Result::OK) {
            LOGD("MetronomeEngine::startStream stream created!");
            // Base timing on the rate the stream was actually granted, not the
            // request (a Bluetooth device may run at a different rate).
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
                LOGD("MetronomeEngine::startStream stream started!");
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
    }
    return result;
}

void MetronomeEngine::onErrorAfterClose(oboe::AudioStream * /*oboeStream*/, oboe::Result error) {
    LOGW("MetronomeEngine::onErrorAfterClose -> %s", oboe::convertToText(error));
    // Ignore if the user has stopped; only recover from an unexpected disconnect.
    if (!isPlaying.load(std::memory_order_relaxed)) {
        return;
    }
    std::lock_guard<std::mutex> lock(mLock);
    // Oboe has already closed the stream. Rebuild on the now-current output
    // device without touching the transport, so the click continues in place.
    stream.reset();
    oboe::Result result = startStream();
    if (result != oboe::Result::OK) {
        LOGE("MetronomeEngine::onErrorAfterClose failed to restart: %s",
             oboe::convertToText(result));
        isPlaying = false;
    }
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
                resetVoices();
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

namespace {

float envelope(float t, float duration) {
    float attack = 0.002f;
    float release = 0.008f;
    if (t < attack) return t / attack;
    else if (t > duration - release) return (duration - t) / release;
    else return 0.9f;
}

constexpr float kBlipSeconds = 0.01f;

int voiceDurationSamples(int voice, double sampleRate) {
    switch (1 << voice) {
        case MetronomeEngine::VOICE_BLIP_HI:
        case MetronomeEngine::VOICE_BLIP_LO:
            return static_cast<int>(sampleRate * kBlipSeconds);
        default:
            return 0;
    }
}

float renderBlip(int age, double sampleRate, float freq, float volume) {
    const float duration = static_cast<int>(sampleRate * kBlipSeconds) / sampleRate;
    const float t = static_cast<float>(age) / sampleRate;
    return volume * envelope(t, duration) * sinf(2.0f * M_PI * freq * t);
}

float renderVoice(int voice, int age, double sampleRate) {
    switch (1 << voice) {
        case MetronomeEngine::VOICE_BLIP_HI: return renderBlip(age, sampleRate, 1760.0f, 0.5f);
        case MetronomeEngine::VOICE_BLIP_LO: return renderBlip(age, sampleRate, 880.0f, 0.3f);
        default: return 0.0f;
    }
}

// Per-beat level (2 = accent, 1 = normal, 0 = mute) as a voice mask.
int blipForLevel(int level) {
    if (level == 2) return MetronomeEngine::VOICE_BLIP_HI;
    if (level == 1) return MetronomeEngine::VOICE_BLIP_LO;
    return 0;
}

// Sample offset of a sub-step inside a beat. Relative to the beat, so the
// fractional carry in beatPhase keeps the sub-steps drift-free as well.
int stepStartSample(int step, double samplesPerBeat, int stepsPerBeat) {
    return static_cast<int>(std::floor(step * samplesPerBeat / stepsPerBeat));
}

} // namespace

void MetronomeEngine::resetVoices() {
    for (int &age : voiceAge) age = -1;
    nextStep = 0;
    activeStepsPerBeat = 1;
    metronomeStyle = true;
}

void MetronomeEngine::strikeVoices(int mask) {
    for (int v = 0; v < NUM_VOICES; ++v) {
        if (mask & (1 << v)) voiceAge[v] = 0;
    }
}

float MetronomeEngine::renderVoices() {
    float out = 0.0f;
    for (int v = 0; v < NUM_VOICES; ++v) {
        if (voiceAge[v] < 0) continue;
        out += renderVoice(v, voiceAge[v], sampleRate);
        if (++voiceAge[v] >= voiceDurationSamples(v, sampleRate)) voiceAge[v] = -1;
    }
    return out;
}

int MetronomeEngine::voicesForStep(int beat, int step) const {
    if (beat < 1 || beat > MAX_BEATS) return 0;
    if (isCountingIn.load(std::memory_order_relaxed)) {
        // The count-in is a steady accent-on-1 click whatever the style.
        return blipForLevel(beat == 1 ? 2 : 1);
    }
    if (metronomeStyle) {
        return blipForLevel(accentPattern[beat - 1].load(std::memory_order_relaxed));
    }
    return grooveStepVoices[(beat - 1) * activeStepsPerBeat + step].load(std::memory_order_relaxed);
}

void MetronomeEngine::generateTick(float *buffer, int32_t numFrames) {
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

            // Latch the groove for this beat so a live change from the JNI
            // thread cannot move the step grid mid-beat.
            const int grooveSteps = grooveStepsPerBeat.load(std::memory_order_acquire);
            metronomeStyle = isCountingIn.load(std::memory_order_relaxed) || grooveSteps == 0;
            activeStepsPerBeat = metronomeStyle ? 1 : grooveSteps;
            nextStep = 0;
        }

        if (nextStep < activeStepsPerBeat &&
            samplesSinceBeat >= stepStartSample(nextStep, period, activeStepsPerBeat)) {
            if (!isSilent.load(std::memory_order_relaxed)) {
                strikeVoices(voicesForStep(currentBeat.load(std::memory_order_relaxed), nextStep));
            }
            nextStep++;
        }

        buffer[i] = renderVoices();

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

void MetronomeEngine::setAccentPattern(const int *pattern, int count) {
    for (int i = 0; i < MAX_BEATS; ++i) {
        accentPattern[i].store(i < count ? pattern[i] : 1, std::memory_order_relaxed);
    }
}

void MetronomeEngine::setGroove(int stepsPerBeat, const int *stepVoices, int count) {
    const int steps = std::min(std::max(stepsPerBeat, 0), MAX_STEPS_PER_BEAT);
    for (int i = 0; i < MAX_STEPS; ++i) {
        grooveStepVoices[i].store(i < count ? stepVoices[i] : 0, std::memory_order_relaxed);
    }
    // Release after the steps so the audio thread never pairs a new step count
    // with stale step voices.
    grooveStepsPerBeat.store(steps, std::memory_order_release);
}

