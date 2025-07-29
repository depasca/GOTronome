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
val ADVANCED_MODE = booleanPreferencesKey("advanced_mode")

val counterSequence = sequenceOf(5, 8, 13, 21)
val timeSignatures = listOf("4/4", "3/4", "2/4", "2/2", "6/8")

class UserPreferencesRepository (
    private val context: Context
) {
    val advancedModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ADVANCED_MODE] ?: false
        }

    val numSilentMeasuresFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[NUM_SILENT_MEASURES] ?: 0
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

    val showBarsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SHOW_BARS] ?: false
        }

    val numBarsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[NUM_BARS] ?: 4
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

    suspend fun setAdvancedMode(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ADVANCED_MODE] = value
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


}