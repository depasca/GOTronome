#ifndef GOTRONOME_VOICES_H
#define GOTRONOME_VOICES_H

// Synthesized percussion voices. Every voice is a pure function of the number
// of samples since it was struck, so the audio thread needs no per-voice state
// beyond that counter. No Android or Oboe dependency: this header is also built
// on the host to render the voices for listening and level checks.

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

inline float hatPedal(float t, int age) {
    return 0.3f * decay(t, 0.01f) * brightNoise(age, HAT_PEDAL);
}

inline float ride(float t, int age) {
    const float wash = 0.12f * decay(t, 0.18f) * brightNoise(age, RIDE);
    const float ping = 0.16f * decay(t, 0.12f) *
                       (sinf(2.0f * kPi * 3140.0f * t) + 0.6f * sinf(2.0f * kPi * 4710.0f * t) +
                        0.4f * sinf(2.0f * kPi * 6280.0f * t));
    return wash + ping;
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
        case HAT_PEDAL: return 0.05f;
        case RIDE: return 0.70f;
        case CROSS_STICK: return 0.06f;
        default: return 0.0f;
    }
}

inline int durationSamples(int voiceIndex, double sampleRate) {
    return static_cast<int>(sampleRate * durationSeconds(1 << voiceIndex));
}

// Sample `age` of voice number `voiceIndex` (0..NUM_VOICES-1).
inline float render(int voiceIndex, int age, double sampleRate) {
    const float t = static_cast<float>(age) / sampleRate;
    switch (1 << voiceIndex) {
        case BLIP_HI: return blip(age, sampleRate, 1760.0f, 0.5f);
        case BLIP_LO: return blip(age, sampleRate, 880.0f, 0.3f);
        case KICK: return kick(t);
        case SNARE: return snare(t, age);
        case HAT_CLOSED: return hatClosed(t, age);
        case HAT_PEDAL: return hatPedal(t, age);
        case RIDE: return ride(t, age);
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
