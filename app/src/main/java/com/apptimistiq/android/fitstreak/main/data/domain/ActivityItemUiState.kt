package com.apptimistiq.android.fitstreak.main.data.domain


enum class ActivityType { WATER, STEP, SLEEP, EXERCISE }

data class ActivityItemUiState(
    val dataType: ActivityType,
    val currentReading: Int = 0,

    )