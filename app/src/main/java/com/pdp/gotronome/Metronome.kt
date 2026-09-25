package com.pdp.gotronome

import android.util.Log

open class Metronome {
    companion object {
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

    object Voice {
        const val BLIP_HI = 1 shl 0
        const val BLIP_LO = 1 shl 1
    }
}