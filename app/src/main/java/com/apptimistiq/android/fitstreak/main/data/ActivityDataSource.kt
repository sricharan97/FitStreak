package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.GoalType
import com.apptimistiq.android.fitstreak.main.dashboard.GoalUserInfo
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.*
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for accessing Activity data
 */
interface ActivityDataSource {

    fun getTodayActivity(): Flow<List<ActivityItemUiState>?>
    fun getWeekActivities(): Flow<List<Activity>>
    fun getCurrentGoals(): Flow<GoalPreferences>
    fun getCurrentUserInfo(): Flow<UserInfoPreferences>
    fun getCurrentGoalUserInfo(goalUserInfo: GoalUserInfo): Flow<Int>
    fun getCurrentActivityVal(activityType: ActivityType): Flow<Int>
    fun getCurrentUserState(): Flow<UserStateInfo>

    suspend fun saveActivity(activityItems: List<ActivityItemUiState>, date: Long)
    suspend fun updateActivity(activityItems: List<ActivityItemUiState>, date: Long)
    suspend fun saveGoal(goalType: GoalType, value: Int)
    suspend fun saveGoalInfo(goalInfoType: GoalUserInfo, value: Int)
    suspend fun saveUserState(userStateInfo: UserStateInfo)

}