package com.pdp.gotronome

import android.util.Log

open class Metronome {
    companion object {
        /** Rest marker in a bass line. */
        const val BASS_REST = -1000
        val TAG = "GOT-Metronome"
        private var callback: MetronomeCallback? = null

        init {
            System.loadLibrary("gotronome") // Load native lib
        }

        @JvmStatic
        fun onNativeBeat(beat: Int) {
            Log.d(TAG, "Beat: $beat")
            callback?.onBeat(beat)
        }
    }

    fun setCallback(cb: MetronomeCallback) {
        callback = cb
        Log.d(TAG, "Setting callback")
    }

    open external fun startMetronome(bpm: Int, beatsPerMeasure: Int)
    open external fun stopMetronome()
    open external fun getCurrentTimeSeconds(): Double
    open external fun getCurrentBeat(): Int
    open external fun getPlayingState(): Int
    open external fun setSilentMeasuresEnabled(enabled: Boolean)
    open external fun setNumSilentMeasures(numSilentMeasures: Int)
    open external fun setCountInEnabled(enabled: Boolean)
    open external fun setAccentPattern(pattern: IntArray)

    /**
     * A groove is [stepsPerBeat] sub-steps per beat, each a bitmask of [Voice]s, laid out
     * beat-major for the whole measure. `stepsPerBeat = 0` selects the Metronome style,
     * one blip per beat from the accent pattern.
     */
    open external fun setGroove(stepsPerBeat: Int, stepVoices: IntArray)

    /**
     * Give voice number [voiceIndex] (0-based bit position of a [Voice]) a recorded one-shot.
     * Mono frames in -1..1 at [sampleRate]; [baseMidiNote] is the recording's pitch for pitched
     * voices, 0 for drums. Ignored by the engine while playing.
     */
    open external fun loadSample(voiceIndex: Int, frames: FloatArray, sampleRate: Int, baseMidiNote: Int)

    /**
     * A bass line: [stepsPerBeat] sub-steps per beat over [bars] measures, each entry a semitone
     * offset from the root or [BASS_REST]. Plays only with drum styles and when enabled.
     */
    open external fun setBassLine(stepsPerBeat: Int, bars: Int, notes: IntArray)
    open external fun setBassRoot(midiNote: Int)
    open external fun setBassEnabled(enabled: Boolean)

    object Voice {
        const val BLIP_HI = 1 shl 0
        const val BLIP_LO = 1 shl 1
        const val KICK = 1 shl 2
        const val SNARE = 1 shl 3
        const val HAT_CLOSED = 1 shl 4
        const val HAT_PEDAL = 1 shl 5
        const val RIDE = 1 shl 6
        const val CROSS_STICK = 1 shl 7
        const val BASS = 1 shl 8
    }

}