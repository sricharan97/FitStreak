package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.GoalType
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.*
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for accessing activity-related data in the FitStreak application.
 *
 * This interface defines the contract for interactions with activity tracking functionality,
 * including retrieving, saving and updating user activities, goals, and state information.
 * It abstracts the underlying data sources (local database, preferences, etc.) from the rest
 * of the application.
 */
interface ActivityDataSource {

    /**
     * Retrieves today's tracked activities as a flow of UI states.
     *
     * @return [Flow] emitting a list of [ActivityItemUiState] objects representing today's activities,
     *         or null if no activities are available
     */
    fun getTodayActivity(): Flow<List<ActivityItemUiState>?>

    /**
     * Retrieves activities tracked throughout the current week.
     *
     * @return [Flow] emitting a list of [Activity] objects for the current week
     */
    fun getWeekActivities(): Flow<List<Activity>>

    /**
     * Retrieves the user's current fitness goal preferences.
     *
     * @return [Flow] emitting [GoalPreferences] containing the user's goal settings
     */
    fun getCurrentGoals(): Flow<GoalPreferences>

    /**
     * Retrieves the user's current profile information preferences.
     *
     * @return [Flow] emitting [UserInfoPreferences] containing user profile data
     */
    fun getCurrentUserInfo(): Flow<UserInfoPreferences>

    /**
     * Retrieves a specific goal-related user information value.
     *
     * @param goalUserInfo The type of goal-user information to retrieve
     * @return [Flow] emitting the requested information as an integer value
     */
    fun getCurrentGoalUserInfo(goalUserInfo: GoalUserInfo): Flow<Int>

    /**
     * Retrieves the current value for a specific activity type.
     *
     * @param activityType The type of activity to query
     * @return [Flow] emitting the current value for the specified activity type as an integer
     */
    fun getCurrentActivityVal(activityType: ActivityType): Flow<Int>

    /**
     * Retrieves the user's current state information.
     *
     * @return [Flow] emitting [UserStateInfo] containing the user's current state
     */
    fun getCurrentUserState(): Flow<UserStateInfo>

    //endregion

    //region Write Operations

    /**
     * Saves a list of activities for the specified date.
     *
     * @param activityItems List of activity items to save
     * @param date Timestamp representing when these activities occurred
     */
    suspend fun saveActivity(activityItems: List<ActivityItemUiState>, date: Long)

    /**
     * Updates existing activities for the specified date.
     *
     * @param activityItems List of activity items with updated data
     * @param date Timestamp representing when these activities occurred
     */
    suspend fun updateActivity(activityItems: List<ActivityItemUiState>, date: Long)

    /**
     * Saves or updates a specific user goal.
     *
     * @param goalType Type of goal being saved
     * @param value Target value for the goal
     */
    suspend fun saveGoal(goalType: GoalType, value: Int)

    /**
     * Saves or updates specific goal-related user information.
     *
     * @param goalInfoType Type of goal info being saved
     * @param value The value to save
     */
    suspend fun saveGoalInfo(goalInfoType: GoalUserInfo, value: Int)

    /**
     * Saves or updates the user's current state information.
     *
     * @param userStateInfo The user state information to save
     */
    suspend fun saveUserState(userStateInfo: UserStateInfo)

    //endregion
}
