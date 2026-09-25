#ifndef GOTRONOME_STRIKEPOOL_H
#define GOTRONOME_STRIKEPOOL_H

// Fixed pool of sounding strikes. Each strike is (voice, samples since struck),
// so the same voice can ring several times over, e.g. a ride cymbal's tail
// carrying through the next hit. No allocation; the audio thread owns it.

#include "Samples.h"
#include "Voices.h"

namespace voices {

constexpr int MAX_STRIKES = 32;

struct Strike {
    int voice = 0;  // voice index 0..NUM_VOICES-1
    int age = -1;   // samples since struck, -1 = free slot
    float state[STRIKE_STATE] = {};  // voice-owned memory, e.g. filter history
};

struct StrikePool {
    Strike slots[MAX_STRIKES];
};

inline void resetPool(StrikePool &pool) {
    for (Strike &s : pool.slots) s.age = -1;
}

// Start every voice in `mask`; when the pool is full the oldest strike is replaced.
inline void strike(StrikePool &pool, int mask) {
    for (int v = 0; v < NUM_VOICES; ++v) {
        if (!(mask & (1 << v))) continue;
        Strike *target = nullptr;
        for (Strike &s : pool.slots) {
            if (s.age < 0) { target = &s; break; }
            if (target == nullptr || s.age > target->age) target = &s;
        }
        target->voice = v;
        target->age = 0;
        for (float &f : target->state) f = 0.0f;
    }
}

// One output sample: the soft-limited sum of every sounding strike, then advance
// them. A voice with a recorded sample in `bank` plays that instead of its synth.
inline float renderPool(StrikePool &pool, double sampleRate, const SampleBank &bank) {
    float out = 0.0f;
    for (Strike &s : pool.slots) {
        if (s.age < 0) continue;
        const bool sampled = hasSample(bank, s.voice);
        out += sampled ? playSample(bank.voices[s.voice], s.age, sampleRate)
                       : render(s.voice, s.age, sampleRate, s.state);
        const int duration = sampled ? sampleDurationSamples(bank.voices[s.voice], sampleRate)
                                     : durationSamples(s.voice, sampleRate);
        if (++s.age >= duration) s.age = -1;
    }
    return softLimit(out);
}

} // namespace voices

#endif // GOTRONOME_STRIKEPOOL_H
