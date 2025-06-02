#include "MetronomeEngine.h"
#include <cmath>
#include <chrono>
#include <android/log.h>

MetronomeEngine::MetronomeEngine() {
    isPlaying = false;
    currentBeat = 0;
}

MetronomeEngine::~MetronomeEngine() {
    stop();
}

oboe::Result MetronomeEngine::start(int _beatsPerMinute, int _beatsPerMeasure) {
    std::lock_guard<std::mutex> lock(mLock);
    LOGD("MetronomeEngine::start");
    this->beatsPerMinute = _beatsPerMinute;
    this->beatsPerMeasure = _beatsPerMeasure;
    samplesPerBeat = (sampleRate * 60.0) / beatsPerMinute;

    oboe::Result result = oboe::Result::OK;
    int tryCount = 0;
    do {
        if (tryCount > 0) {
            usleep(20 * 1000); // Sleep between tries to give the system time to settle.
        }
        result = createStream();
        if (result == oboe::Result::OK) {
            LOGD("MetronomeEngine::start stream created!");
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
                currentBeat = 0;
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
    LOGD("MetronomeEngine::generateTick start, numFrames -> %d, currentBeat -> %d", numFrames, currentBeat);
    const float tickVolume = 0.3f;
    const float accentVolume = 0.5f;
    const int tickLength = static_cast<int>(sampleRate * 0.01); // 10ms tick
    static int frameCounter = 0;

    for (int i = 0; i < numFrames; ++i) {
        // Trigger beat at start of each beat window
        if ((frameCounter % static_cast<int>(samplesPerBeat)) == 0) {
//            sendBeatToJava(currentBeat);
            currentBeat += 1;
            if (currentBeat > beatsPerMeasure) currentBeat = 1;
        }

        int beatOffset = frameCounter % static_cast<int>(samplesPerBeat);
        bool isTick = beatOffset < tickLength;

        float freq = (currentBeat == 1) ? 1760.0f : 880.0f;
        float volume = (currentBeat == 1) ? accentVolume : tickVolume;

        if (isTick) {
            float t = static_cast<float>(beatOffset) / sampleRate;
            float env = envelope(t, tickLength / sampleRate);
            buffer[i] = volume * env * sinf(2.0f * M_PI * freq * t);
        } else {
            buffer[i] = 0.0f;
        }

        frameCounter++;
    }
}

double MetronomeEngine::getCurrentTimeSeconds() {
    return 0; //static_cast<double>(frameCounter) / sampleRate;
}

int MetronomeEngine::getCurrentBeat() const {
    return currentBeat;
}

jboolean MetronomeEngine::getIisPlaying() {
    return isPlaying;
}

oboe::DataCallbackResult MetronomeEngine::onAudioReady(oboe::AudioStream *_stream,
                                                       void *audioData,
                                                       int32_t numFrames) {
    float *floatData = static_cast<float *>(audioData);
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

