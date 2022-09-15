package com.apptimistiq.android.fitstreak.main.data.domain

data class ProgressTrackUiState(
    val activityList: List<ActivityItemUiState> = emptyList(),
    val isFetchingActivities: Boolean = false,
    val userMessages: String? = null,
    val canAccessGoogleFit: Boolean = false,
    val subscriptionDone: Boolean = false,
    val readSteps: Boolean = false,
    val readCalories: Boolean = false,
    val readWaterLitres: Boolean = false,
    val readSleepHrs: Boolean = false,
    val activitySavedForDay: Boolean = false
)


