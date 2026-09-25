#ifndef GOTRONOME_VOICES_H
#define GOTRONOME_VOICES_H

// Synthesized percussion voices. Every voice is a function of the number of
// samples since it was struck plus a small, explicitly passed state block for
// voices that need filter memory. No Android or Oboe dependency: this header
// is also built on the host to render the voices for listening and level checks.

#include <cmath>
#include <cstdint>

namespace voices {

enum Voice : int {
    BLIP_HI = 1 << 0,
    BLIP_LO = 1 << 1,
    KICK = 1 << 2,
    SNARE = 1 << 3,
    HAT_CLOSED = 1 << 4,
    HAT_PEDAL = 1 << 5,
    RIDE = 1 << 6,
    CROSS_STICK = 1 << 7,
};

constexpr int NUM_VOICES = 8;
constexpr int STRIKE_STATE = 24;  // floats of per-strike memory available to a voice
constexpr float kPi = 3.14159265358979f;
constexpr float kBlipSeconds = 0.01f;

inline float blipEnvelope(float t, float duration) {
    float attack = 0.002f;
    float release = 0.008f;
    if (t < attack) return t / attack;
    else if (t > duration - release) return (duration - t) / release;
    else return 0.9f;
}

inline float decay(float t, float tau) { return expf(-t / tau); }

// Deterministic white noise: a hash of (sample index, voice) in [-1, 1].
inline float noise(int age, int voice) {
    uint32_t h = static_cast<uint32_t>(age) * 0x9E3779B1u ^ static_cast<uint32_t>(voice) * 0x85EBCA77u;
    h ^= h >> 15; h *= 0x2C1B3C6Du; h ^= h >> 12; h *= 0x297A2D39u; h ^= h >> 15;
    return static_cast<float>(h) / 2147483648.0f - 1.0f;
}

// First difference of the noise: a cheap first-order high-pass for bright, tinny sounds.
inline float brightNoise(int age, int voice) {
    return 0.5f * (noise(age, voice) - noise(age - 1, voice));
}

inline float blip(int age, double sampleRate, float freq, float volume) {
    const float duration = static_cast<int>(sampleRate * kBlipSeconds) / sampleRate;
    const float t = static_cast<float>(age) / sampleRate;
    return volume * blipEnvelope(t, duration) * sinf(2.0f * kPi * freq * t);
}

// Sine whose pitch falls from `fStart` to `fEnd` with time constant `tau`; the
// phase is the closed-form integral of that frequency curve.
inline float sweptSine(float t, float fStart, float fEnd, float tau) {
    const float phase = 2.0f * kPi * (fEnd * t + (fStart - fEnd) * tau * (1.0f - expf(-t / tau)));
    return sinf(phase);
}

inline float kick(float t) {
    return 0.7f * decay(t, 0.09f) * sweptSine(t, 160.0f, 48.0f, 0.03f);
}

inline float snare(float t, int age) {
    const float body = 0.4f * decay(t, 0.04f) * sinf(2.0f * kPi * 185.0f * t);
    const float wires = 0.3f * decay(t, 0.06f) * noise(age, SNARE);
    return body + wires;
}

inline float hatClosed(float t, int age) {
    return 0.5f * decay(t, 0.015f) * brightNoise(age, HAT_CLOSED);
}

// Foot "chick": darker and rounder than the stick hat, with enough body to mark 2 and 4.
inline float hatPedal(float t, int age) {
    const float dark = 0.6f * noise(age, HAT_PEDAL) + 0.4f * brightNoise(age, HAT_PEDAL);
    return 0.5f * decay(t, 0.025f) * dark;
}

// Two-pole resonator on `st` = {c1, c2, y1, y2}. Output is normalised by (1 - r)
// so the peak gain is about one whatever the bandwidth.
inline void tuneResonator(float *st, float freq, float bandwidth, double sampleRate) {
    const float r = expf(-kPi * bandwidth / static_cast<float>(sampleRate));
    st[0] = 2.0f * r * cosf(2.0f * kPi * freq / static_cast<float>(sampleRate));
    st[1] = -r * r;
    st[2] = 0.0f;
    st[3] = 0.0f;
}

inline float resonate(float *st, float x, float bandwidth, double sampleRate) {
    const float y = x + st[0] * st[2] + st[1] * st[3];
    st[3] = st[2];
    st[2] = y;
    const float r = expf(-kPi * bandwidth / static_cast<float>(sampleRate));
    return (1.0f - r) * y;
}

// Ride cymbal. A cymbal is thousands of modes, far closer to shaped noise than
// to a chord of partials (that recipe is a bell). So: white noise excited by a
// hard stick burst plus a wash that swells briefly and decays over a second or
// two, run through five resonant bands in the 2-9 kHz shimmer region, with a
// slow amplitude wobble for movement and a very short sine ping for the stick.
constexpr int kRideBands = 5;
constexpr float kRideFreq[kRideBands] = {1900.0f, 3100.0f, 4600.0f, 6400.0f, 8900.0f};
constexpr float kRideBandwidth[kRideBands] = {260.0f, 380.0f, 520.0f, 700.0f, 1000.0f};
constexpr float kRideTau[kRideBands] = {1.20f, 1.00f, 0.80f, 0.60f, 0.42f};
constexpr float kRideGain[kRideBands] = {0.9f, 1.0f, 0.9f, 0.7f, 0.5f};

inline float ride(float t, int age, double sampleRate, float *state) {
    if (age == 0) {
        for (int b = 0; b < kRideBands; ++b) tuneResonator(state + 4 * b, kRideFreq[b], kRideBandwidth[b], sampleRate);
    }
    const float excite = noise(age, RIDE);
    const float stick = 1.6f * decay(t, 0.010f);
    const float swell = 1.0f - decay(t, 0.025f);
    float out = 0.0f;
    for (int b = 0; b < kRideBands; ++b) {
        const float env = stick + swell * decay(t, kRideTau[b]);
        out += kRideGain[b] * resonate(state + 4 * b, excite * env, kRideBandwidth[b], sampleRate);
    }
    const float wobble = 1.0f + 0.18f * sinf(2.0f * kPi * 5.3f * t) + 0.10f * sinf(2.0f * kPi * 8.9f * t);
    const float air = 0.05f * decay(t, 0.7f) * brightNoise(age, RIDE);
    const float ping = 0.12f * decay(t, 0.03f) * (sinf(2.0f * kPi * 3620.0f * t) + 0.6f * sinf(2.0f * kPi * 5410.0f * t));
    return 0.55f * out * wobble + air + ping;
}

inline float crossStick(float t, int age) {
    const float wood = 0.35f * decay(t, 0.012f) *
                       (sinf(2.0f * kPi * 1250.0f * t) + 0.5f * sinf(2.0f * kPi * 2600.0f * t));
    const float click = 0.15f * decay(t, 0.004f) * noise(age, CROSS_STICK);
    return wood + click;
}

inline float durationSeconds(int voiceBit) {
    switch (voiceBit) {
        case BLIP_HI:
        case BLIP_LO: return kBlipSeconds;
        case KICK: return 0.30f;
        case SNARE: return 0.25f;
        case HAT_CLOSED: return 0.07f;
        case HAT_PEDAL: return 0.12f;
        case RIDE: return 2.00f;
        case CROSS_STICK: return 0.06f;
        default: return 0.0f;
    }
}

inline int durationSamples(int voiceIndex, double sampleRate) {
    return static_cast<int>(sampleRate * durationSeconds(1 << voiceIndex));
}

// Sample `age` of voice number `voiceIndex` (0..NUM_VOICES-1). `state` is the
// strike's STRIKE_STATE floats, zeroed when it was struck.
inline float render(int voiceIndex, int age, double sampleRate, float *state) {
    const float t = static_cast<float>(age) / sampleRate;
    switch (1 << voiceIndex) {
        case BLIP_HI: return blip(age, sampleRate, 1760.0f, 0.5f);
        case BLIP_LO: return blip(age, sampleRate, 880.0f, 0.3f);
        case KICK: return kick(t);
        case SNARE: return snare(t, age);
        case HAT_CLOSED: return hatClosed(t, age);
        case HAT_PEDAL: return hatPedal(t, age);
        case RIDE: return ride(t, age, sampleRate, state);
        case CROSS_STICK: return crossStick(t, age);
        default: return 0.0f;
    }
}

// Transparent below the knee, then a smooth tanh shoulder that never reaches
// full scale. The metronome blips peak at 0.45 and pass through untouched.
inline float softLimit(float x) {
    constexpr float knee = 0.8f;
    const float mag = fabsf(x);
    if (mag <= knee) return x;
    const float shaped = knee + (1.0f - knee) * tanhf((mag - knee) / (1.0f - knee));
    return x < 0 ? -shaped : shaped;
}

} // namespace voices

#endif // GOTRONOME_VOICES_H
