package com.pdp.gotronome

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pdp.gotronome.data.FOURFOURS
import com.pdp.gotronome.data.MODE_BAR_LOOP
import com.pdp.gotronome.data.MODE_BASIC
import com.pdp.gotronome.data.MODE_SILENT_BARS
import com.pdp.gotronome.data.SIXEIGHTS
import com.pdp.gotronome.data.THREEFOURS
import com.pdp.gotronome.data.TWOFOURS
import com.pdp.gotronome.data.TWOTWOS
import com.pdp.gotronome.data.UserPreferencesRepository
import com.pdp.gotronome.data.counterSequence
import com.pdp.gotronome.data.modes
import com.pdp.gotronome.data.timeSignatures
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "GOT-Settings"
const val PLAYING_STATE_PLAYING = 1
const val PLAYING_STATE_STOPPED = 0
const val PLAYING_STATE_SILENT = 2

open class MetronomeViewModel(
    private val userPreferencesRepository: UserPreferencesRepository?,
    private val metronome: Metronome?
): ViewModel(), MetronomeCallback {

    private val _page = MutableStateFlow<String>("settings")
    val page: StateFlow<String> = _page

    private val _currentBeat = MutableStateFlow<Int>(0)
    private val _currentBar = MutableStateFlow<Int>(0)

    private val _beatsPerMeasure = MutableStateFlow<Int>(4)
    val beatsPerMeasure: StateFlow<Int> = _beatsPerMeasure

    private val _beatsPerMinute = MutableStateFlow<Int>(100)
    open val beatsPerMinute: StateFlow<Int> = _beatsPerMinute

    private val _timeSignature = MutableStateFlow<String>(timeSignatures.first())
    val timeSignature: StateFlow<String> = _timeSignature

    private val _mode = MutableStateFlow<String>(modes.first())
    val mode: StateFlow<String> = _mode

    private val _numRuns = MutableStateFlow<Int>(0)
    val numRuns: StateFlow<Int> = _numRuns

    private val _reviewPromptCounter = MutableStateFlow<Int>(counterSequence.first())
    val reviewPromptCounter: StateFlow<Int> = _reviewPromptCounter

    private val _showBars = MutableStateFlow<Boolean>(false)
    open val showBars: StateFlow<Boolean> = _showBars

    private val _numBars = MutableStateFlow<Int>(4)
    open val numBars: StateFlow<Int> = _numBars

    private val _numSilentMeasures = MutableStateFlow<Int>(0)
    open val numSilentMeasures: StateFlow<Int> = _numSilentMeasures

    init {
        initialize()
    }
    open fun initialize() {
        Log.d(TAG, "MetronomeViewModel init")
        metronome!!.setCallback(this)

        viewModelScope.launch {
            Log.d(TAG, "Collecting initial user preferences")

            // Read reviewPromptCounter once
            _reviewPromptCounter.value = userPreferencesRepository!!.reviewPromptCounterFlow.first()
            Log.d(TAG, "Init -> Review prompt counter: ${_reviewPromptCounter.value}")

            // Read numRuns once
            _numRuns.value = userPreferencesRepository.numRunsFlow.first()
            Log.d(TAG, "Init -> Num runs: ${_numRuns.value}")

            // Read beatsPerMinute once
            _beatsPerMinute.value = userPreferencesRepository.beatsPerMinuteFlow.first()
            Log.d(TAG, "Init -> Beats per minute: ${_beatsPerMinute.value}")

            // Read timeSignature once
            val initialTimeSignature = userPreferencesRepository.timeSignatureFlow.first()
            _timeSignature.value = initialTimeSignature
            updateBeatsPerMeasure() // Ensure this is called after _timeSignature is set
            Log.d(TAG, "Init -> Time signature: $initialTimeSignature")

            _showBars.value = userPreferencesRepository.showBarsFlow.first()
            Log.d(TAG, "Init -> Show bars: ${_showBars.value}")

            _numBars.value = userPreferencesRepository.numBarsFlow.first()
            Log.d(TAG, "Init -> Num bars: ${_numBars.value}")

            val initialNumSilentMeasures = userPreferencesRepository.numSilentMeasuresFlow.first()
            _numSilentMeasures.value = initialNumSilentMeasures
            metronome.setNumSilentMeasures(initialNumSilentMeasures)
            Log.d(TAG, "Init -> Num silent measures: $initialNumSilentMeasures")

            val initialMode = userPreferencesRepository.modeFlow.first()
            _mode.value = initialMode
            metronome.setSilentMeasuresEnabled(initialMode == MODE_SILENT_BARS)
            Log.d(TAG, "Init -> Mode: $initialMode")
        }
        Log.d(TAG, "MetronomeViewModel init done")
    }

    open fun setPage(page: String) {
        _page.value = page
    }

    open fun start() {
        metronome!!.startMetronome(_beatsPerMinute.value, _beatsPerMeasure.value)
    }

    open fun stop() {
        metronome!!.stopMetronome()
        _currentBeat.value = 0
        _currentBar.value = 0
    }

    open fun getIsPlaying(): Int {
        return metronome!!.getPlayingState()
    }

    override fun onBeat(beatIndex: Int) {
        Log.d(TAG, "Beat: $beatIndex")
    }

    open fun getCurrentBeat(): Int {
        val prevBeat = _currentBeat.value
        _currentBeat.value = metronome!!.getCurrentBeat()
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
        _timeSignature.value = timeSignature
        updateBeatsPerMeasure()

        viewModelScope.launch {
            Log.d(TAG, "Setting beats per measure to: ${_beatsPerMeasure.value}")
            userPreferencesRepository!!.setTimeSignature(_timeSignature.value)
        }
    }

    fun updateBeatsPerMeasure() {
        _beatsPerMeasure.value = when (_timeSignature.value) {
            FOURFOURS -> 4
            THREEFOURS -> 3
            TWOFOURS -> 2
            TWOTWOS -> 2
            SIXEIGHTS -> 6
            else -> 4
        }
    }

    open fun setMode(mode: String) {
        Log.d(TAG, "Setting mode to: $mode")
        _mode.value = mode
        viewModelScope.launch {
            userPreferencesRepository?.setMode(_mode.value)
        }
        when (_mode.value) {
            MODE_BAR_LOOP -> {
                setShowBars(true)
                metronome?.setSilentMeasuresEnabled(false)
            }
            MODE_SILENT_BARS -> {
                setShowBars(false)
                metronome?.setNumSilentMeasures(_numSilentMeasures.value)
                metronome?.setSilentMeasuresEnabled(true)
            }
            MODE_BASIC -> {
                setShowBars(false)
                metronome?.setSilentMeasuresEnabled(false)
            }
        }
    }

    open fun setBeatsPerMinute(value: Int) {
        _beatsPerMinute.value = value
    }

    open fun setShowBars(value: Boolean) {
        _showBars.value = value
        viewModelScope.launch {
            userPreferencesRepository!!.setShowBars(_showBars.value)
        }
    }

    open fun setNumBars(value: Int) {
        _numBars.value = value
    }

    open fun storeBeatsPerMinute() {
        viewModelScope.launch {
            userPreferencesRepository!!.setBeatsPerMinute(_beatsPerMinute.value)
        }
    }

    open fun storeNumBars(){
        viewModelScope.launch {
            userPreferencesRepository!!.setNumBars(_numBars.value)
        }
    }
    open fun storeNumSilentMeasures() {
        viewModelScope.launch {
            userPreferencesRepository!!.setNumSilentBars(_numSilentMeasures.value)
        }
    }

    open fun incrementNumRuns() {
        viewModelScope.launch {
            userPreferencesRepository!!.incrementNumRuns()
        }
        _numRuns.value++
    }

    open fun incrementReviewPromptCounter() {
        viewModelScope.launch {
            userPreferencesRepository!!.incrementReviewPromptCounter()
            userPreferencesRepository.reviewPromptCounterFlow.collect { value ->
                _reviewPromptCounter.value = value
            }
        }
    }

    open fun resetNumRuns() {
        viewModelScope.launch {
            userPreferencesRepository!!.resetNumRuns()
        }
        _numRuns.value = 0
    }

    open fun setNumSilentMeasures(value: Int) {
        _numSilentMeasures.value = value
        metronome!!.setNumSilentMeasures(_numSilentMeasures.value)
        viewModelScope.launch {
            userPreferencesRepository!!.setNumSilentBars(_numSilentMeasures.value)
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