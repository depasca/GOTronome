package com.pdp.gotronome

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pdp.gotronome.data.UserPreferencesRepository
import com.pdp.gotronome.data.counterSequence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "GOT-MetronomeViewModel"

open class MetronomeViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val metronome: Metronome
): ViewModel(), MetronomeCallback {

    private val _page = MutableStateFlow<String>("settings")
    val page: StateFlow<String> = _page

    private val _currentBeat = MutableStateFlow<Int>(0)

    private val _beatsPerMeasure = MutableStateFlow<Int>(4)
    val beatsPerMeasure: StateFlow<Int> = _beatsPerMeasure

    private val _beatsPerMinute = MutableStateFlow<Int>(100)
    open val beatsPerMinute: StateFlow<Int> = _beatsPerMinute

    val timeSignatures = listOf("4/4", "3/4", "2/4", "2/2", "6/8")

    private val _selectedTimeSignature = MutableStateFlow<String>(timeSignatures[0])
    val selectedTimeSignature: StateFlow<String> = _selectedTimeSignature

    private val _numRuns = MutableStateFlow<Int>(0)
    val numRuns: StateFlow<Int> = _numRuns

    private val _reviewPromptCounter = MutableStateFlow<Int>(counterSequence.first())
    val reviewPromptCounter: StateFlow<Int> = _reviewPromptCounter

    init {
        metronome.setCallback(this)
    }

    open fun setPage(page: String) {
        _page.value = page
    }

    open fun start() {
        metronome.startMetronome(_beatsPerMinute.value, _beatsPerMeasure.value)
    }

    open fun stop() {
        metronome.stopMetronome()
    }

    open fun getIsPlaying(): Boolean {
        return metronome.getIsPLaying()
    }

    override fun onBeat(beatIndex: Int) {
        Log.d(TAG, "Beat: $beatIndex")
    }

    open fun getCurrentBeat(): Int {
        _currentBeat.value = metronome.getCurrentBeat()
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

    open fun incrementNumRuns() {
        viewModelScope.launch {
            userPreferencesRepository.incrementNumRuns()
        }
        _numRuns.value++
    }

    open fun incrementReviewPromptCounter() {
        viewModelScope.launch {
            userPreferencesRepository.incrementReviewPromptCounter()
        }
    }

    open fun resetNumRuns() {
        viewModelScope.launch {
            userPreferencesRepository.resetNumRuns()
        }
        _numRuns.value = 0
    }

}

class MetronomeViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val metronome: Metronome
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MetronomeViewModel::class.java)) {
            return MetronomeViewModel(userPreferencesRepository, metronome) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}