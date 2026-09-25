#ifndef GOTRONOME_SAMPLES_H
#define GOTRONOME_SAMPLES_H

// Optional recorded one-shots. A voice with a sample plays it instead of its
// synthesized version. The bank only points at storage the engine owns; it is
// filled before the stream starts and never touched from the audio thread.

#include "Voices.h"

namespace voices {

struct Sample {
    const float *data = nullptr;  // mono, full scale +-1
    int length = 0;               // frames; 0 = no sample, use the synth voice
    float rate = 48000.0f;        // frames per second the sample was recorded at
};

struct SampleBank {
    Sample voices[NUM_VOICES];
};

inline bool hasSample(const SampleBank &bank, int voiceIndex) {
    return bank.voices[voiceIndex].length > 0;
}

inline int sampleDurationSamples(const Sample &s, double sampleRate) {
    return static_cast<int>(s.length * sampleRate / s.rate);
}

// Linear-interpolated playback of `s`, resampled from its own rate to `sampleRate`.
inline float playSample(const Sample &s, int age, double sampleRate) {
    const double pos = age * (s.rate / sampleRate);
    const int i = static_cast<int>(pos);
    if (i + 1 >= s.length) return i < s.length ? s.data[i] : 0.0f;
    const float frac = static_cast<float>(pos - i);
    return s.data[i] + frac * (s.data[i + 1] - s.data[i]);
}

} // namespace voices

#endif // GOTRONOME_SAMPLES_H
