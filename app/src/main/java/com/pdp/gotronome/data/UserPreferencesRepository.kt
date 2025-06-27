package com.pdp.gotronome.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)
val NUM_RUNS = intPreferencesKey("num_runs")
val REVIEW_PROMPT_COUNTER = intPreferencesKey("review_prompt_counter")
val counterSequence = sequenceOf(5, 8, 13, 21)

class UserPreferencesRepository (
    private val context: Context
) {
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
            var numRuns = preferences[REVIEW_PROMPT_COUNTER] ?: counterSequence.first()
            if(numRuns != counterSequence.last()) {
                preferences[REVIEW_PROMPT_COUNTER] = counterSequence.first { it > numRuns }
            }
        }
    }
}