package com.apptimistiq.android.fitstreak.main.data.domain


data class ActivityItemUiState(
    val dataType: ActivityType,
    val currentReading: Int = 0,
    val goalReading: Int = 0
    )

