package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for accessing Activity data
 */
interface ActivityDataSource {

    fun getTodayActivity(): Flow<List<ActivityItemUiState>>
    fun getWeekActivities(): Flow<List<Activity>>

    suspend fun saveActivity(activityItems: List<ActivityItemUiState>, date: Long)
    suspend fun updateActivity(activityItems: List<ActivityItemUiState>, date: Long)

}