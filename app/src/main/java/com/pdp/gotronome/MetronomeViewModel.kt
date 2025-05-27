package com.pdp.gotronome

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "GOT-MetronomeViewModel"

open class MetronomeViewModel(): ViewModel(), MetronomeCallback {
    private var _metronome: Metronome? = null

    private val _currentBeat = MutableStateFlow<Int>(0)
    val currentBeat: StateFlow<Int> = _currentBeat

    private val _timeSignature = MutableStateFlow<TimeSignature>(TimeSignature.FOUR_QUARTERS)
    val timeSignature: StateFlow<TimeSignature> = _timeSignature

    private val _beatsPerMeasure = MutableStateFlow<Int>(4)
    val beatsPerMeasure: StateFlow<Int> = _beatsPerMeasure

    private val _beatsPerMinute = MutableStateFlow<Int>(100)
    open val beatsPerMinute: StateFlow<Int> = _beatsPerMinute

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    val timeSignatures = listOf("4/4", "3/4", "2/4")

    private val _selectedTimeSignature = MutableStateFlow<String>(timeSignatures[0])
    val selectedTimeSignature: StateFlow<String> = _selectedTimeSignature

    init {
        _metronome?.setCallback(this)
    }

    open fun start() {
        _metronome!!.startMetronome(_beatsPerMinute.value, _beatsPerMeasure.value)
        _isPlaying.value = true
    }

    open fun stop() {
        _metronome!!.stopMetronome()
        _currentBeat.value = 0
        _isPlaying.value = false
    }

    open fun getIsPlaying(): Boolean {
        return _metronome!!.getIsPLaying()
    }

    override fun onBeat(beatIndex: Int) {
        _currentBeat.value = beatIndex
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
            else -> 4
        }
    }

    open fun setBeatsPerMinute(value: Int) {
        _beatsPerMinute.value = value
    }

    fun setIsplaying(value: Boolean) {
        _isPlaying.value = value
    }

    fun setMetronome(metronome: Metronome) {
        _metronome = metronome
    }
}