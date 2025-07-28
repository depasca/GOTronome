package com.pdp.gotronome

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pdp.gotronome.data.UserPreferencesRepository
import com.pdp.gotronome.data.counterSequence
import com.pdp.gotronome.data.timeSignatures
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "GOT-Settings"
const val PLAYING_STATE_PLAYING = 1
const val PLAYING_STATE_STOPPED = 0
const val PLAYING_STATE_SILENT = 2

open class MetronomeViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val metronome: Metronome
): ViewModel(), MetronomeCallback {

    private val _page = MutableStateFlow<String>("settings")
    val page: StateFlow<String> = _page

    private val _currentBeat = MutableStateFlow<Int>(0)
    private val _currentBar = MutableStateFlow<Int>(0)

    private val _beatsPerMeasure = MutableStateFlow<Int>(4)
    val beatsPerMeasure: StateFlow<Int> = _beatsPerMeasure

    private val _beatsPerMinute = MutableStateFlow<Int>(100)
    open val beatsPerMinute: StateFlow<Int> = _beatsPerMinute

    private val _selectedTimeSignature = MutableStateFlow<String>(timeSignatures.first())
    val selectedTimeSignature: StateFlow<String> = _selectedTimeSignature

    private val _numRuns = MutableStateFlow<Int>(0)
    val numRuns: StateFlow<Int> = _numRuns

    private val _reviewPromptCounter = MutableStateFlow<Int>(counterSequence.first())
    val reviewPromptCounter: StateFlow<Int> = _reviewPromptCounter

    private val _showBars = MutableStateFlow<Boolean>(false)
    val showBars: StateFlow<Boolean> = _showBars

    private val _numBars = MutableStateFlow<Int>(4)
    val numBars: StateFlow<Int> = _numBars

    private val _numSilentMeasures = MutableStateFlow<Int>(0)
    val numSilentMeasures: StateFlow<Int> = _numSilentMeasures

    init {
        Log.d(TAG, "MetronomeViewModel init")
        metronome.setCallback(this)
        viewModelScope.launch {
            Log.d(TAG, "Collecting user preferences")
            userPreferencesRepository.reviewPromptCounterFlow.collect { value ->
                Log.d(TAG, "Init ->Review prompt counter: $value")
                _reviewPromptCounter.value = value
            }
        }
        viewModelScope.launch {

            userPreferencesRepository.numRunsFlow.collect { value ->
                Log.d(TAG, "Init ->Num runs: $value")
                _numRuns.value = value
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.beatsPerMinuteFlow.collect { value ->
                Log.d(TAG, "Init ->Beats per minute: $value")
                _beatsPerMinute.value = value
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.timeSignatureFlow.collect { value ->
                Log.d(TAG, "Init -> Time signature: $value")
                _selectedTimeSignature.value = value
                updateBeatsPerMeasure()
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.showBarsFlow.collect { value ->
                Log.d(TAG, "Init -> Show bars: $value")
                _showBars.value = value
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.numBarsFlow.collect { value ->
                Log.d(TAG, "Init -> Num bars: $value")
                _numBars.value = value
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.numSilentMeasuresFlow.collect { value ->
                Log.d(TAG, "Init -> Num silent measures: $value")
                _numSilentMeasures.value = value
                metronome.setNumSilentMeasures(_numSilentMeasures.value)
            }
        }
        Log.d(TAG, "MetronomeViewModel init done")
    }

    open fun setPage(page: String) {
        _page.value = page
    }

    open fun start() {
        metronome.startMetronome(_beatsPerMinute.value, _beatsPerMeasure.value)
    }

    open fun stop() {
        metronome.stopMetronome()
        _currentBeat.value = 0
        _currentBar.value = 0
    }

    open fun getIsPlaying(): Int {
        return metronome.getPlayingState()
    }

    override fun onBeat(beatIndex: Int) {
        Log.d(TAG, "Beat: $beatIndex")
    }

    open fun getCurrentBeat(): Int {
        val prevBeat = _currentBeat.value
        _currentBeat.value = metronome.getCurrentBeat()
        if(prevBeat != _currentBeat.value && _currentBeat.value == 1) {
            _currentBar.value++
            if(_currentBar.value > _numBars.value) {
                _currentBar.value = 1
            }
        }
        return _currentBeat.value
    }

    fun getCurrentBar(): Int {
        return _currentBar.value
    }

    open fun setTimeSignature(timeSignature: String) {
        _selectedTimeSignature.value = timeSignature
        updateBeatsPerMeasure()

        viewModelScope.launch {
            Log.d(TAG, "Setting beats per measure to: ${_beatsPerMeasure.value}")
            userPreferencesRepository.setTimeSignature(_selectedTimeSignature.value)
        }
    }

    fun updateBeatsPerMeasure() {
        _beatsPerMeasure.value = when (_selectedTimeSignature.value) {
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

    open fun setShowBars(value: Boolean) {
        _showBars.value = value
        viewModelScope.launch {
            userPreferencesRepository.setShowBars(_showBars.value)
        }
    }

    open fun setNumBars(value: Int) {
        _numBars.value = value
    }

    fun storeBeatsPerMinute() {
        viewModelScope.launch {
            userPreferencesRepository.setBeatsPerMinute(_beatsPerMinute.value)
        }
    }

    fun storeNumBars(){
        viewModelScope.launch {
            userPreferencesRepository.setNumBars(_numBars.value)
        }
    }
    open fun storeNumSilentMeasures() {
        viewModelScope.launch {
            userPreferencesRepository.setNumSilentBars(_numSilentMeasures.value)
        }
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
            userPreferencesRepository.reviewPromptCounterFlow.collect { value ->
                _reviewPromptCounter.value = value
            }
        }
    }

    open fun resetNumRuns() {
        viewModelScope.launch {
            userPreferencesRepository.resetNumRuns()
        }
        _numRuns.value = 0
    }

    open fun setNumSilentMeasures(value: Int) {
        _numSilentMeasures.value = value
        metronome.setNumSilentMeasures(_numSilentMeasures.value)
        viewModelScope.launch {
            userPreferencesRepository.setNumSilentBars(_numSilentMeasures.value)
            }
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