#ifndef METRONOMEENGINE_H
#define METRONOMEENGINE_H

#include <oboe/Oboe.h>
#include <memory>
#include <atomic>
#include <jni.h>
#include "StrikePool.h"
#include <vector>

#define MODULE_NAME  "GOT-CPP"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, MODULE_NAME, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, MODULE_NAME, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, MODULE_NAME, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, MODULE_NAME, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, MODULE_NAME, __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, MODULE_NAME, __VA_ARGS__)

class MetronomeEngine : public oboe::AudioStreamCallback {
public:
    static constexpr int MAX_BEATS = 16;
    static constexpr int MAX_STEPS_PER_BEAT = 4;
    static constexpr int NUM_VOICES = voices::NUM_VOICES;
    static constexpr int MAX_STEPS = MAX_BEATS * MAX_STEPS_PER_BEAT;
    static constexpr int MAX_BASS_BARS = 4;
    static constexpr int MAX_BASS_STEPS = MAX_STEPS * MAX_BASS_BARS;
    static constexpr int BASS_REST = -1000;

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

    // A groove is stepsPerBeat sub-steps per beat, each a voice bitmask, for a
    // whole measure (beat-major order). stepsPerBeat == 0 selects the Metronome
    // style: one blip per beat chosen from the accent pattern.
    void setGroove(int stepsPerBeat, const int *stepVoices, int count);

    // Give a voice a recorded one-shot (mono float frames at `rate`). Must be
    // called while stopped: the audio thread reads the bank without a lock.
    // `baseMidiNote` is the recording's pitch for pitched voices, 0 otherwise.
    void loadSample(int voiceIndex, const float *frames, int length, int rate, int baseMidiNote);

    // A bass line is stepsPerBeat sub-steps per beat over `bars` measures, each
    // a semitone offset from the root or BASS_REST. It cycles with the measure
    // counter independently of the drum groove. Silent with the Metronome style.
    void setBassLine(int stepsPerBeat, int bars, const int *notes, int count);
    void setBassRoot(int midiNote);
    void setBassEnabled(bool enabled);

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
    std::atomic<int> grooveStepsPerBeat{0};     // written from the JNI thread; 0 = Metronome style
    std::atomic<int> grooveStepVoices[MAX_STEPS]; // written from the JNI thread
    std::atomic<int> bassStepsPerBeat{0};       // written from the JNI thread; 0 = no bass line
    std::atomic<int> bassBars{1};
    std::atomic<int> bassNotes[MAX_BASS_STEPS];
    std::atomic<int> bassRoot{36};              // MIDI note of the root, C2 by default
    std::atomic<bool> bassEnabled{false};
    int activeBassStepsPerBeat = 0;       // audio thread only: bass sub-steps in the current beat
    int activeBassBars = 1;               // audio thread only
    int nextBassStep = 0;                 // audio thread only
    int activeStepsPerBeat = 1;           // audio thread only: sub-steps in the current beat
    int nextStep = 0;                     // audio thread only: next sub-step to strike
    bool metronomeStyle = true;           // audio thread only: latched at each beat
    voices::StrikePool strikes;           // audio thread only: every sounding strike
    voices::SampleBank samples;           // filled while stopped, read by the audio thread
    std::vector<float> sampleStorage[NUM_VOICES];
    std::mutex mLock;

    JavaVM *javaVm = nullptr;
    jobject javaCallbackObj = nullptr;
    jmethodID onBeatMethod = nullptr;

    oboe::Result createStream();
    oboe::Result startStream(); // open + start with retries; assumes mLock held
    void generateTick(float *buffer, int32_t numFrames);
    void resetVoices();
    int voicesForStep(int beat, int step) const;
    int bassNoteForStep(int beat, int step) const;
    float bassRateFor(int midiNote) const;
    void sendBeatToJava(int beat);

};

#endif // METRONOMEENGINE_H
