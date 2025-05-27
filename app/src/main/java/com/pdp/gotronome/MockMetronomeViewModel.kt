package com.pdp.gotronome

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockMetronomeViewModel : MetronomeViewModel() {
    override val beatsPerMinute: StateFlow<Int> = MutableStateFlow(120)
    override fun setBeatsPerMinute(bpm: Int) {}
    override fun setTimeSignature(timeSignature: String) {}
    override fun start() {}
    override fun stop() {}
    override fun getCurrentBeat(): Int {return 1}
}