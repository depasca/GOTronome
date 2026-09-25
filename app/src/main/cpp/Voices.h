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
    BASS = 1 << 8,
};

constexpr int NUM_VOICES = 9;

// Bit position of a single Voice flag, e.g. voiceIndex(RIDE) == 6.
inline int voiceIndex(int voiceBit) {
    int index = 0;
    while (index < NUM_VOICES - 1 && !(voiceBit & (1 << index))) ++index;
    return index;
}
constexpr int STRIKE_STATE = 200;  // floats of per-strike memory available to a voice
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

// Modal resonator on `st` = {c1, c2, y1, y2, gain}: a two-pole filter that, hit
// with an impulse, rings as a decaying sine at `freq` with amplitude time
// constant `tau`. Costs a few multiply-adds per sample instead of a sine call.
inline void tuneMode(float *st, float freq, float tau, float amplitude, double sampleRate) {
    const float sr = static_cast<float>(sampleRate);
    const float r = expf(-1.0f / (tau * sr));
    const float theta = 2.0f * kPi * freq / sr;
    st[0] = 2.0f * r * cosf(theta);
    st[1] = -r * r;
    st[2] = 0.0f;
    st[3] = 0.0f;
    st[4] = amplitude * sinf(theta);  // an impulse rings with peak 1/sin(theta); undo that
}

inline float ringMode(float *st, float x) {
    const float y = x + st[0] * st[2] + st[1] * st[3];
    st[3] = st[2];
    st[2] = y;
    return st[4] * y;
}

// Ride cymbal by modal synthesis. A cymbal is a dense set of narrow, inharmonic
// modes ringing for a second or more: a handful of long partials is a bell,
// wide noisy bands are hiss, so this uses forty modes on a jittered log grid
// from 700 Hz to 10 kHz, weighted towards the 3-4 kHz shimmer region, with the
// low ones ringing longest. A short noise burst excites them, so phases are
// random and neighbouring modes beat; a faint noise trickle keeps the wash alive.
constexpr int kRideModes = 40;
constexpr int kRideModeFloats = 5;

inline void tuneRide(float *state, double sampleRate) {
    for (int k = 0; k < kRideModes; ++k) {
        const float pos = static_cast<float>(k) / (kRideModes - 1);
        const float grid = 700.0f * powf(10000.0f / 700.0f, pos);
        const float freq = grid * (1.0f + 0.10f * noise(k, RIDE));
        const float tau = 1.4f * powf(700.0f / freq, 0.6f);
        const float logRatio = logf(freq / 3500.0f);
        const float weight = expf(-logRatio * logRatio / (2.0f * 0.7f * 0.7f)) + 0.25f;
        const float amplitude = weight * (1.0f + 0.3f * noise(k + 100, RIDE));
        tuneMode(state + kRideModeFloats * k, freq, tau, amplitude, sampleRate);
    }
}

inline float ride(float t, int age, double sampleRate, float *state) {
    if (age == 0) tuneRide(state, sampleRate);
    const float burst = decay(t, 0.004f);
    const float trickle = 0.006f * decay(t, 0.5f);
    const float excite = noise(age, RIDE) * (burst + trickle);
    float out = 0.0f;
    for (int k = 0; k < kRideModes; ++k) out += ringMode(state + kRideModeFloats * k, excite);
    const float stick = 0.15f * decay(t, 0.003f) * brightNoise(age, RIDE);
    return 0.008f * out + stick;
}

// Pitched voices play one note at a time: a new strike fades the previous one out.
inline bool isMonophonic(int voiceIndex) { return (1 << voiceIndex) == BASS; }

// Placeholder plucked bass at A1 when no recorded note is loaded; `rate` is the
// pitch multiplier, 2^(semitones/12). Harmonics decay faster the higher they are.
constexpr float kBassBaseHz = 55.0f;

inline float bass(float t, int age, float rate) {
    const float f = kBassBaseHz * rate;
    constexpr float amps[6] = {1.0f, 0.5f, 0.33f, 0.2f, 0.12f, 0.08f};
    float body = 0.0f;
    for (int n = 1; n <= 6; ++n) body += amps[n - 1] * decay(t, 0.9f / n) * sinf(2.0f * kPi * f * n * t);
    const float attack = 1.0f - decay(t, 0.003f);
    const float thump = 0.25f * decay(t, 0.01f) * noise(age, BASS);
    return 0.35f * body * attack + thump;
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
        case BASS: return 1.5f;
        default: return 0.0f;
    }
}

inline int durationSamples(int voiceIndex, double sampleRate) {
    return static_cast<int>(sampleRate * durationSeconds(1 << voiceIndex));
}

// Sample `age` of voice number `voiceIndex` (0..NUM_VOICES-1). `state` is the
// strike's STRIKE_STATE floats, zeroed when it was struck; `rate` the pitch
// multiplier for pitched voices.
inline float render(int voiceIndex, int age, double sampleRate, float *state, float rate) {
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
        case BASS: return bass(t, age, rate);
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
