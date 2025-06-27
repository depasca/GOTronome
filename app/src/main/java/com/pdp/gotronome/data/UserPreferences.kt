package com.pdp.gotronome.data

data class UserPreferences(
    val beatsPerMeasure: Int,
    val beatsPerMinute: Int,
    val numRuns: Int,
    val reviewed: Boolean
)
