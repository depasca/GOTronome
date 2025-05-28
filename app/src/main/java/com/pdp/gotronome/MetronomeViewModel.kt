package com.pdp.gotronome

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "GOT-MetronomeViewModel"

open class MetronomeViewModel(): ViewModel(), MetronomeCallback {
    private var _metronome: Metronome? = null

    private val _currentBeat = MutableStateFlow<Int>(0)

    private val _beatsPerMeasure = MutableStateFlow<Int>(4)
    val beatsPerMeasure: StateFlow<Int> = _beatsPerMeasure

    private val _beatsPerMinute = MutableStateFlow<Int>(100)
    open val beatsPerMinute: StateFlow<Int> = _beatsPerMinute

    val timeSignatures = listOf("4/4", "3/4", "2/4", "2/2", "6/8")

    private val _selectedTimeSignature = MutableStateFlow<String>(timeSignatures[0])
    val selectedTimeSignature: StateFlow<String> = _selectedTimeSignature

    init {
        _metronome?.setCallback(this)
    }

    open fun start() {
        _metronome!!.startMetronome(_beatsPerMinute.value, _beatsPerMeasure.value)
    }

    open fun stop() {
        _metronome!!.stopMetronome()
    }

    open fun getIsPlaying(): Boolean {
        return _metronome!!.getIsPLaying()
    }

    override fun onBeat(beatIndex: Int) {
        Log.d(TAG, "Beat: $beatIndex")
    }

    open fun getCurrentBeat(): Int {
        _currentBeat.value = _metronome!!.getCurrentBeat()
        return _currentBeat.value
    }

    open fun setTimeSignature(timeSignature: String) {
        _selectedTimeSignature.value = timeSignature
        _beatsPerMeasure.value = when (timeSignature) {
            "4/4" -> 4
            "3/4" -> 3
            "2/4" -> 2
            "2/2" -> 2
            "6/8" -> 6
            else -> 4
        }
    }

    open fun setBeatsPerMinute(value: Int) {
        _beatsPerMinute.value = value
    }

    fun setMetronome(metronome: Metronome) {
        _metronome = metronome
    }
}