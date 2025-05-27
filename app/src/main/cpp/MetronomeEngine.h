#ifndef METRONOMEENGINE_H
#define METRONOMEENGINE_H

#include <oboe/Oboe.h>
#include <memory>
#include <atomic>
#include <jni.h>


class MetronomeEngine : public oboe::AudioStreamCallback {
public:
    MetronomeEngine();
    ~MetronomeEngine() override;
    void setJavaVM(JavaVM *vm, jobject callbackObject);

    void start(int _beatsPerMinute, int beatsPerMesure);
    void pause();
    void stop();
    static double getCurrentTimeSeconds();
    int getCurrentBeat() const;

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *stream,
                                          void *audioData,
                                          int32_t numFrames) override;

    bool getIisPlaying();

private:
    std::shared_ptr<oboe::AudioStream> stream;
    bool isPlaying = false;
    int beatsPerMinute;
    double sampleRate = 48000.0;
    double samplesPerBeat = 0.0;
    int currentBeat = 0;
//    int frameCounter = 0;
    int beatsPerMeasure = 4;

    JavaVM *javaVm = nullptr;
    jobject javaCallbackObj = nullptr;
    jmethodID onBeatMethod = nullptr;

    void createStream();
    void generateTick(float *buffer, int32_t numFrames);
    void sendBeatToJava(int beat);

};

#endif // METRONOMEENGINE_H
