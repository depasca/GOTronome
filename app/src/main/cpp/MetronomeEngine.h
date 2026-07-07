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
    static const int MAX_BEATS = 16;

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

    // Called by Oboe when the stream is disconnected (e.g. Bluetooth or wired
    // headphones connect/disconnect mid-session); we rebuild on the new device.
    void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error) override;

    int getPlayingState();
    void setNumSilentMeasures(int numSilentMeasures);

    void setSilentMeasuresEnabled(bool b);

    void setCountInEnabled(bool b);

    void setAccentPattern(const int *pattern, int count);

private:
    std::shared_ptr<oboe::AudioStream> stream;
    std::atomic<bool> isPlaying{false};
    int beatsPerMinute = 0;
    double sampleRate = 48000.0;
    double samplesPerBeat = 0.0;          // only written in start(), before the stream runs
    std::atomic<int> currentBeat{0};      // read from the UI thread
    int currentMeasure = 0;
    int beatsPerMeasure = 4;
    std::atomic<int> silentMeasures{0};   // written live from the JNI thread
    int silentMeasureCounter = 0;
    std::atomic<bool> silentMeasureEnabled{false}; // written live from the JNI thread
    std::atomic<bool> isSilent{false};    // read from the UI thread
    std::atomic<bool> countInEnabled{true}; // written from the JNI thread
    std::atomic<bool> isCountingIn{false}; // read from the UI thread
    int countInBeats = 0;                 // audio thread only: beats in the lead-in bar
    int countInBeat = 0;                  // audio thread only: current lead-in beat 1..N
    double beatPhase = 0.0;               // audio thread only: samples until next beat
    int samplesSinceBeat = 0;             // audio thread only: samples since last beat
    // Per-beat level: 2 = accent, 1 = normal, 0 = mute. Written from the JNI thread.
    std::atomic<int> accentPattern[MAX_BEATS];
    std::mutex mLock;

    JavaVM *javaVm = nullptr;
    jobject javaCallbackObj = nullptr;
    jmethodID onBeatMethod = nullptr;

    oboe::Result createStream();
    oboe::Result startStream(); // open + start with retries; assumes mLock held
    void generateTick(float *buffer, int32_t numFrames);
    void sendBeatToJava(int beat);

};

#endif // METRONOMEENGINE_H
