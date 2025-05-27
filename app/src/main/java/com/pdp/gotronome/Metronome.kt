package com.pdp.gotronome

import android.content.res.AssetManager
import android.util.Log
import java.io.IOException

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
    open external fun getIsPLaying(): Boolean
}