#ifndef GOTRONOME_STRIKEPOOL_H
#define GOTRONOME_STRIKEPOOL_H

// Fixed pool of sounding strikes. Each strike is (voice, samples since struck),
// so the same voice can ring several times over, e.g. a ride cymbal's tail
// carrying through the next hit. No allocation; the audio thread owns it.

#include "Samples.h"
#include "Voices.h"
#include <algorithm>

namespace voices {

constexpr int MAX_STRIKES = 32;

constexpr float kReleaseSeconds = 0.008f;  // fade when a monophonic voice is re-struck

struct Strike {
    int voice = 0;       // voice index 0..NUM_VOICES-1
    int age = -1;        // samples since struck, -1 = free slot
    int releaseAge = -1; // age at which the fade-out began, -1 = not releasing
    float rate = 1.0f;   // pitch multiplier for pitched voices
    float state[STRIKE_STATE] = {};  // voice-owned memory, e.g. filter history
};

struct StrikePool {
    Strike slots[MAX_STRIKES];
};

inline void resetPool(StrikePool &pool) {
    for (Strike &s : pool.slots) s.age = -1;
}

// Start every voice in `mask` at pitch `rate`; when the pool is full the oldest
// strike is replaced. A monophonic voice fades out its previous strike first.
inline void strike(StrikePool &pool, int mask, float rate = 1.0f) {
    for (int v = 0; v < NUM_VOICES; ++v) {
        if (!(mask & (1 << v))) continue;
        if (isMonophonic(v)) {
            for (Strike &s : pool.slots) {
                if (s.age >= 0 && s.voice == v && s.releaseAge < 0) s.releaseAge = s.age;
            }
        }
        Strike *target = nullptr;
        for (Strike &s : pool.slots) {
            if (s.age < 0) { target = &s; break; }
            if (target == nullptr || s.age > target->age) target = &s;
        }
        target->voice = v;
        target->age = 0;
        target->releaseAge = -1;
        target->rate = rate;
        for (float &f : target->state) f = 0.0f;
    }
}

// One output sample: the soft-limited sum of every sounding strike, then advance
// them. A voice with a recorded sample in `bank` plays that instead of its synth.
inline float renderPool(StrikePool &pool, double sampleRate, const SampleBank &bank) {
    float out = 0.0f;
    const int releaseSamples = static_cast<int>(kReleaseSeconds * sampleRate);
    for (Strike &s : pool.slots) {
        if (s.age < 0) continue;
        const bool sampled = hasSample(bank, s.voice);
        float value = sampled ? playSample(bank.voices[s.voice], s.age, sampleRate, s.rate)
                              : render(s.voice, s.age, sampleRate, s.state, s.rate);
        int duration = sampled ? sampleDurationSamples(bank.voices[s.voice], sampleRate, s.rate)
                               : durationSamples(s.voice, sampleRate);
        if (s.releaseAge >= 0) {
            value *= 1.0f - static_cast<float>(s.age - s.releaseAge) / releaseSamples;
            duration = std::min(duration, s.releaseAge + releaseSamples);
        }
        out += value;
        if (++s.age >= duration) s.age = -1;
    }
    return softLimit(out);
}

} // namespace voices

#endif // GOTRONOME_STRIKEPOOL_H
