#include "MetronomeEngine.h"
#include <cmath>
#include <chrono>
#include <android/log.h>

#define LOG_TAG "GOTCPP"

MetronomeEngine::MetronomeEngine() {
    isPlaying = false;
    currentBeat = 0;
}

MetronomeEngine::~MetronomeEngine() {
    stop();
}

void MetronomeEngine::start(int _beatsPerMinute, int _beatsPerMeasure) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::start");
    this->beatsPerMinute = _beatsPerMinute;
    this->beatsPerMeasure = _beatsPerMeasure;
    samplesPerBeat = (sampleRate * 60.0) / beatsPerMinute;
    if(!isPlaying) {
        if (stream) {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::start stream already created!");
            stream->requestStart();
        }
        else {
            __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::start creating new stream");
            createStream();
            if (stream) stream->requestStart();
        }
        isPlaying = true;
    }
}

void MetronomeEngine::pause() {
    if (isPlaying) {
        if (stream) stream->requestPause();
        isPlaying = false;
        currentBeat = 0;
    }
}

void MetronomeEngine::stop() {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::stop");
    if(isPlaying){
        if (stream) stream->requestStop();
        isPlaying = false;
        currentBeat = 0;
    }
}

void MetronomeEngine::createStream() {
    oboe::AudioStreamBuilder builder;
    builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setChannelCount(1)
            ->setSampleRate(static_cast<int>(sampleRate))
            ->setCallback(this)
            ->setDirection(oboe::Direction::Output);

    auto result = builder.openStream(stream);
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::createStream result -> %d", result);
    if (stream) sampleRate = stream->getSampleRate();
}

float envelope(float t, float duration) {
    float attack = 0.002f;
    float release = 0.008f;
    if (t < attack) return t / attack;
    else if (t > duration - release) return (duration - t) / release;
    else return 0.9f;
}

void MetronomeEngine::generateTick(float *buffer, int32_t numFrames) {
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::generateTick start, numFrames -> %d, currentBeat -> %d", numFrames, currentBeat);
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
//    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::generateTick end, numFrames -> %d, currentBeat -> %d", numFrames, currentBeat);
}

double MetronomeEngine::getCurrentTimeSeconds() {
    return 0; //static_cast<double>(frameCounter) / sampleRate;
}

int MetronomeEngine::getCurrentBeat() const {
//    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::getCurrentBeat -> %d", currentBeat);
    return currentBeat;
}

bool MetronomeEngine::getIisPlaying() {
//    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "MetronomeEngine::getIisPlaying -> %d", isPlaying);
    return isPlaying;
}

oboe::DataCallbackResult MetronomeEngine::onAudioReady(oboe::AudioStream *_stream,
                                                       void *audioData,
                                                       int32_t numFrames) {
    if (!isPlaying) return oboe::DataCallbackResult::Stop;
    float *floatData = static_cast<float *>(audioData);
    generateTick(floatData, numFrames);
//    frameCounter = 0;
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
            std::cerr << "GOT-MetronomeEngine Failed to find class Metronome" << std::endl;
        }
        else{
            env->CallStaticVoidMethod(c, onBeatMethod, beat);
        }
    }
}

