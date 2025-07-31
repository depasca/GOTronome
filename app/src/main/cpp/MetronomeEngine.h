#ifndef METRONOMEENGINE_H
#define METRONOMEENGINE_H

#include <oboe/Oboe.h>
#include <memory>
#include <atomic>
#include <jni.h>

#define MODULE_NAME  "GOT-CPP"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, MODULE_NAME, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, MODULE_NAME, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, MODULE_NAME, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, MODULE_NAME, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, MODULE_NAME, __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, MODULE_NAME, __VA_ARGS__)

class MetronomeEngine : public oboe::AudioStreamCallback {
public:
    MetronomeEngine();
    ~MetronomeEngine() override;
    void setJavaVM(JavaVM *vm, jobject callbackObject);

    oboe::Result start(int _beatsPerMinute, int beatsPerMesure);
//    void pause();
    oboe::Result stop();
    static double getCurrentTimeSeconds();
    [[nodiscard]] int getCurrentBeat() const;

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *stream,
                                          void *audioData,
                                          int32_t numFrames) override;

    int getPlayingState();
    void setNumSilentMeasures(int numSilentMeasures);

    void setSilentMeasuresEnabled(bool b);

private:
    std::shared_ptr<oboe::AudioStream> stream;
    jboolean isPlaying = false;
    int beatsPerMinute;
    double sampleRate = 48000.0;
    double samplesPerBeat = 0.0;
    int currentBeat = 0;
    int currentMeasure = 0;
    int beatsPerMeasure = 4;
    int silentMeasures = 0;
    int silentMeasureCounter = 0;
    bool silentMeasureEnabled = false;
    bool isSilent = false;
    std::mutex mLock;

    JavaVM *javaVm = nullptr;
    jobject javaCallbackObj = nullptr;
    jmethodID onBeatMethod = nullptr;

    oboe::Result createStream();
    void generateTick(float *buffer, int32_t numFrames);
    void sendBeatToJava(int beat);

};

#endif // METRONOMEENGINE_H
