package com.pdp.gotronome.data

import android.content.Context
import android.util.Log
import com.pdp.gotronome.Metronome

private const val TAG = "GOT-Samples"
private const val SAMPLES_DIR = "samples"

/** Recorded hits are scaled to this peak so they sit with the synth voices instead of over them. */
const val SAMPLE_PEAK = 0.6f

/** Recorded one-shots shipped in assets/samples, by file name → voice bit. Missing files keep the synth voice. */
val sampleFiles: Map<String, Int> = mapOf(
    "ride.wav" to Metronome.Voice.RIDE,
)

fun voiceIndexOf(voiceBit: Int): Int = Integer.numberOfTrailingZeros(voiceBit)

fun normalized(clip: PcmClip, peak: Float): PcmClip {
    val current = clip.frames.maxOfOrNull { kotlin.math.abs(it) } ?: 0f
    if (current == 0f) return clip
    val gain = peak / current
    return clip.copy(frames = FloatArray(clip.frames.size) { clip.frames[it] * gain })
}

/** Loads every bundled sample into the engine. Call before the metronome starts. */
fun loadSamples(context: Context, metronome: Metronome) {
    val available = context.assets.list(SAMPLES_DIR)?.toSet() ?: emptySet()
    sampleFiles.filterKeys { it in available }.forEach { (file, voiceBit) ->
        val clip = normalized(context.assets.open("$SAMPLES_DIR/$file").use { decodeWav(it.readBytes()) }, SAMPLE_PEAK)
        metronome.loadSample(voiceIndexOf(voiceBit), clip.frames, clip.sampleRate)
        Log.d(TAG, "loaded $file: ${clip.frames.size} frames at ${clip.sampleRate} Hz")
    }
}
