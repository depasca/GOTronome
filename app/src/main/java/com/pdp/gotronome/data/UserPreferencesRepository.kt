package com.pdp.gotronome.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val TAG = "GOT-Settings"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)
val NUM_RUNS = intPreferencesKey("num_runs")
val REVIEW_PROMPT_COUNTER = intPreferencesKey("review_prompt_counter")
val BEATS_PER_MINUTE = intPreferencesKey("beats_per_minute")
val TIME_SIGNATURE = stringPreferencesKey("time_signature")
val SHOW_BARS = booleanPreferencesKey("show_bars")
val NUM_BARS = intPreferencesKey("num_bars")
val NUM_SILENT_MEASURES = intPreferencesKey("num_silent_measures")
val MODE = stringPreferencesKey("mode")
val COUNT_IN_ENABLED = booleanPreferencesKey("count_in_enabled")
val counterSequence = sequenceOf(5, 8, 13, 21)

const val FOURFOURS = "4/4"
const val THREEFOURS = "3/4"
const val TWOFOURS = "2/4"
const val TWOTWOS = "2/2"
const val SIXEIGHTS = "6/8"
val timeSignatures = listOf(FOURFOURS, THREEFOURS, TWOFOURS, TWOTWOS, SIXEIGHTS)

const val MODE_BASIC = "Basic"
const val MODE_SILENT_BARS = "Silent bars"
const val MODE_BAR_LOOP = "Bar loop"
val modes = listOf(MODE_BASIC, MODE_SILENT_BARS, MODE_BAR_LOOP)

const val BEAT_MUTE = 0
const val BEAT_NORMAL = 1
const val BEAT_ACCENT = 2

fun beatsForTimeSignature(timeSignature: String): Int = when (timeSignature) {
    FOURFOURS -> 4
    THREEFOURS -> 3
    TWOFOURS -> 2
    TWOTWOS -> 2
    SIXEIGHTS -> 6
    else -> 4
}

fun defaultAccentPattern(timeSignature: String): List<Int> {
    val beats = beatsForTimeSignature(timeSignature)
    return List(beats) { if (it == 0) BEAT_ACCENT else BEAT_NORMAL }
}

private fun accentPatternKey(timeSignature: String) =
    stringPreferencesKey("accent_pattern_$timeSignature")

class UserPreferencesRepository (
    private val context: Context
) {
    val numSilentMeasuresFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[NUM_SILENT_MEASURES] ?: 1
        }
    val reviewPromptCounterFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[REVIEW_PROMPT_COUNTER] ?: counterSequence.first()
        }

    val numRunsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[NUM_RUNS] ?: 0
        }

    val beatsPerMinuteFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[BEATS_PER_MINUTE] ?: 120
        }

    val timeSignatureFlow: Flow<String> = context.dataStore.data
        .map {
            preferences ->
            (preferences[TIME_SIGNATURE] ?: timeSignatures.first())
        }

    val modeFlow: Flow<String> = context.dataStore.data
        .map {
            preferences ->
            (preferences[MODE] ?: modes.first())
        }

    val showBarsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SHOW_BARS] ?: false
        }

    val numBarsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[NUM_BARS] ?: 4
        }

    val countInEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[COUNT_IN_ENABLED] ?: true
        }

    fun accentPatternFlow(timeSignature: String): Flow<List<Int>> = context.dataStore.data
        .map { preferences ->
            val stored = preferences[accentPatternKey(timeSignature)]
            val parsed = stored?.split(",")?.mapNotNull { it.trim().toIntOrNull() }
            if (parsed != null && parsed.size == beatsForTimeSignature(timeSignature)) parsed
            else defaultAccentPattern(timeSignature)
        }

    suspend fun incrementNumRuns() {
        context.dataStore.edit { preferences ->
            val currentNumRuns = preferences[NUM_RUNS] ?: 0
            preferences[NUM_RUNS] = currentNumRuns + 1
        }
   }

    suspend fun resetNumRuns() {
        context.dataStore.edit { preferences ->
            preferences[NUM_RUNS] = 0
        }
    }

    suspend fun incrementReviewPromptCounter() {
        context.dataStore.edit { preferences ->
            val numRuns = preferences[REVIEW_PROMPT_COUNTER] ?: counterSequence.first()
            if(numRuns != counterSequence.last()) {
                preferences[REVIEW_PROMPT_COUNTER] = counterSequence.find {
                    it > numRuns
                }?: numRuns
            }
        }
    }

    suspend fun setBeatsPerMinute(bpm: Int) {
        Log.d(TAG, "setBeatsPerMinute: $bpm")
        context.dataStore.edit { preferences ->
            preferences[BEATS_PER_MINUTE] = bpm
        }
    }

    suspend fun setTimeSignature(ts: String) {
        context.dataStore.edit { preferences ->
            preferences[TIME_SIGNATURE] = ts
        }
    }

    suspend fun setShowBars(showBars: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_BARS] = showBars
        }
    }

    suspend fun setNumBars(numBars: Int) {
        context.dataStore.edit { preferences ->
            preferences[NUM_BARS] = numBars
        }
    }

    suspend fun setNumSilentBars(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[NUM_SILENT_MEASURES] = value
        }
    }

    suspend fun setMode(value: String) {
        context.dataStore.edit { preferences ->
            preferences[MODE] = value
        }
    }

    suspend fun setCountInEnabled(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[COUNT_IN_ENABLED] = value
        }
    }

    suspend fun setAccentPattern(timeSignature: String, pattern: List<Int>) {
        context.dataStore.edit { preferences ->
            preferences[accentPatternKey(timeSignature)] = pattern.joinToString(",")
        }
    }


}