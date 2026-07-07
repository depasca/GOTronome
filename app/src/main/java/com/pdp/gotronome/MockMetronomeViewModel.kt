package com.pdp.gotronome

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private val TAG = "GOT-MockMetronomeViewModel"

class MockMetronomeViewModel : MetronomeViewModel(null, null) {
    override fun initialize() {
        Log.d(TAG, "MockMetronomeViewModel init")
    }
    override val beatsPerMinute: StateFlow<Int> = MutableStateFlow(120)
    override val numSilentMeasures: StateFlow<Int> = MutableStateFlow(0)
    override val showBars: StateFlow<Boolean> = MutableStateFlow(true)
    override val numBars: StateFlow<Int> = MutableStateFlow(4)
    override val countInEnabled: StateFlow<Boolean> = MutableStateFlow(true)
    override val accentPattern: StateFlow<List<Int>> = MutableStateFlow(listOf(2, 1, 0, 1))

    override fun setBeatsPerMinute(value: Int) {}
    override fun storeBeatsPerMinute() {}
    override fun setTimeSignature(timeSignature: String) {}
    override fun start() {}
    override fun stop() {}
    override fun getCurrentBeat(): Int {return 1}
    override fun incrementNumRuns(){}
    override fun incrementReviewPromptCounter() {}
    override fun resetNumRuns() {}
    override fun setShowBars(value: Boolean) {}
    override fun setNumBars(value: Int) {}
    override fun storeNumBars() {}
    override fun setNumSilentMeasures(value: Int) {}
    override fun storeNumSilentMeasures() {}
    override fun setCountInEnabled(value: Boolean) {}
    override fun cycleAccentBeat(index: Int) {}
    override fun setPage(page: String){}
}