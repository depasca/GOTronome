package com.pdp.gotronome

import com.pdp.gotronome.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockMetronomeViewModel(
    userPreferencesRepository: UserPreferencesRepository,
    metronome: Metronome
) : MetronomeViewModel(
    userPreferencesRepository, metronome
) {
    override val beatsPerMinute: StateFlow<Int> = MutableStateFlow(120)
    override fun setBeatsPerMinute(bpm: Int) {}
    override fun setTimeSignature(timeSignature: String) {}
    override fun start() {}
    override fun stop() {}
    override fun getCurrentBeat(): Int {return 1}
    override fun incrementNumRuns(){}
    override fun incrementReviewPromptCounter() {}
    override fun resetNumRuns() {}
}