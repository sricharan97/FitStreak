/*
 * ActivityItemUiState.kt
 * FitStreak Project
 *
 * UI state representation for user fitness activities
 * Created as part of the fitness tracking domain model
 */
package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * Data class representing the UI state for a fitness activity item.
 * Encapsulates the current reading and goal metrics for various activity types.
 *
 * @property dataType The type of fitness activity (e.g. STEPS, CALORIES etc).
 * @property currentReading The current value achieved for this activity type.
 * @property goalReading The target value set as a goal for this activity type.
 */
data class ActivityItemUiState(
    val dataType: ActivityType,
    val currentReading: Int = 0,
    val goalReading: Int = 0
)
