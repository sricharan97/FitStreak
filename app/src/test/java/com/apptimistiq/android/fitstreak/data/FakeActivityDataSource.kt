package com.apptimistiq.android.fitstreak.data

import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.*
import com.apptimistiq.android.fitstreak.main.data.mappers.asDomainModel
import kotlinx.coroutines.flow.*

/**
 * A fake implementation of [ActivityDataSource] for testing purposes.
 * This class simulates the behavior of the data source without requiring
 * a real database or network connection. It uses MutableStateFlow to
 * create observable, hot data streams.
 */
class FakeActivityDataSource : ActivityDataSource {

    private var shouldReturnError = false

    private val currentDayActivity = MutableStateFlow<Activity?>(
        Activity(
            id = 1,
            dateOfActivity = System.currentTimeMillis(),
            steps = 5000,
            waterGlasses = 3,
            sleepHours = 7,
            exerciseCalories = 2000
        )
    )

    private val currentGoalPreferences = MutableStateFlow(
        GoalPreferences(
            stepGoal = 10000,
            waterGlassGoal = 8,
            sleepGoal = 8,
            exerciseGoal = 3000
        )
    )

    private val weekActivities = MutableStateFlow(
        listOf(
            Activity(id = 1, dateOfActivity = System.currentTimeMillis() - 86400000, steps = 6000, waterGlasses = 4, sleepHours = 6, exerciseCalories = 2500),
            Activity(id = 2, dateOfActivity = System.currentTimeMillis() - 172800000, steps = 7000, waterGlasses = 5, sleepHours = 8, exerciseCalories = 3000),
            Activity(id = 3, dateOfActivity = System.currentTimeMillis() - 259200000, steps = 8000, waterGlasses = 6, sleepHours = 7, exerciseCalories = 3500),
            Activity(id = 4, dateOfActivity = System.currentTimeMillis() - 345600000, steps = 9000, waterGlasses = 7, sleepHours = 5, exerciseCalories = 4000),
            Activity(id = 5, dateOfActivity = System.currentTimeMillis() - 432000000, steps = 10000, waterGlasses = 8, sleepHours = 9, exerciseCalories = 4500),
            Activity(id = 6, dateOfActivity = System.currentTimeMillis() - 518400000, steps = 11000, waterGlasses = 9, sleepHours = 6, exerciseCalories = 5000),
            Activity(id = 7, dateOfActivity = System.currentTimeMillis() - 604800000, steps = 12000, waterGlasses = 10, sleepHours = 8, exerciseCalories = 5500)
        )
    )

    private val currentUserInfo = MutableStateFlow(
        UserInfoPreferences(
            height = 170,
            weight = 70,
        )
    )

    private val currentUserState = MutableStateFlow(
        UserStateInfo(
            uid = "",
            userName = "Test User",
            isUserLoggedIn = false,
            isOnboarded = false
        )
    )

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override fun getTodayActivity(): Flow<List<ActivityItemUiState>?> {
        return currentDayActivity.combine(currentGoalPreferences) { activity, goals ->
            if (shouldReturnError) throw Exception("Test exception")
            activity?.asDomainModel(goals)
        }
    }

    fun setTodayActivity(activity: Activity?) {
        currentDayActivity.value = activity
    }

    fun setCurrentGoalPreferences(preferences: GoalPreferences) {
        currentGoalPreferences.value = preferences
    }

    override fun getWeekActivities(): Flow<List<Activity>> {
        return if (shouldReturnError) {
            flow { throw Exception("Test exception") }
        } else {
            weekActivities
        }
    }

    fun setWeekActivities(activities: List<Activity>) {
        weekActivities.value = activities
    }

    override fun getCurrentGoals(): Flow<GoalPreferences> {
        return currentGoalPreferences.map { if (shouldReturnError) throw Exception("Test exception") else it }
    }

    override fun getCurrentUserInfo(): Flow<UserInfoPreferences> {
        return currentUserInfo.map { if (shouldReturnError) throw Exception("Test exception") else it }
    }

    fun setCurrentUserInfo(userInfo: UserInfoPreferences) {
        currentUserInfo.value = userInfo
    }

    override fun getCurrentGoalUserInfo(goalUserInfo: GoalUserInfo): Flow<Int> {
        return currentUserInfo.combine(currentGoalPreferences) { info, goals ->
            if (shouldReturnError) throw Exception("Test exception")
            when (goalUserInfo) {
                GoalUserInfo.HEIGHT -> info.height
                GoalUserInfo.WEIGHT -> info.weight
                GoalUserInfo.STEPS -> goals.stepGoal
                GoalUserInfo.WATER -> goals.waterGlassGoal
                GoalUserInfo.EXERCISE -> goals.exerciseGoal
                GoalUserInfo.SLEEP -> goals.sleepGoal
                GoalUserInfo.DEFAULT -> 0
            }
        }
    }

    override fun getCurrentActivityVal(activityType: ActivityType): Flow<Int> {
        return currentDayActivity.map { activity ->
            if (shouldReturnError) throw Exception("Test exception")
            when (activityType) {
                ActivityType.STEP -> activity?.steps ?: 0
                ActivityType.WATER -> activity?.waterGlasses ?: 0
                ActivityType.EXERCISE -> activity?.exerciseCalories ?: 0
                ActivityType.SLEEP -> activity?.sleepHours ?: 0
                ActivityType.DEFAULT -> 0
            }
        }
    }

    override fun getCurrentUserState(): Flow<UserStateInfo> {
        return currentUserState.map { if (shouldReturnError) throw Exception("Test exception") else it }
    }

    fun setCurrentUserState(userState: UserStateInfo) {
        currentUserState.value = userState
    }

    override suspend fun saveActivity(activityItems: List<ActivityItemUiState>, date: Long) {
        val baseActivity = currentDayActivity.value ?: Activity(
            dateOfActivity = date,
            steps = 0,
            waterGlasses = 0,
            sleepHours = 0,
            exerciseCalories = 0
        )

        var updatedActivity = baseActivity
        activityItems.forEach { item ->
            updatedActivity = when (item.dataType) {
                ActivityType.STEP -> updatedActivity.copy(steps = item.currentReading)
                ActivityType.WATER -> updatedActivity.copy(waterGlasses = item.currentReading)
                ActivityType.SLEEP -> updatedActivity.copy(sleepHours = item.currentReading)
                ActivityType.EXERCISE -> updatedActivity.copy(exerciseCalories = item.currentReading)
                else -> updatedActivity
            }
        }
        currentDayActivity.value = updatedActivity
    }

    override suspend fun updateActivity(activityItems: List<ActivityItemUiState>, date: Long) {
        saveActivity(activityItems, date)
    }

    override suspend fun saveGoal(goalType: GoalType, value: Int) {
        val currentGoals = currentGoalPreferences.value
        currentGoalPreferences.value = when (goalType) {
            GoalType.STEP -> currentGoals.copy(stepGoal = value)
            GoalType.WATER -> currentGoals.copy(waterGlassGoal = value)
            GoalType.SLEEP -> currentGoals.copy(sleepGoal = value)
            GoalType.EXERCISE -> currentGoals.copy(exerciseGoal = value)
        }
    }

    override suspend fun saveGoalInfo(goalInfoType: GoalUserInfo, value: Int) {
        when (goalInfoType) {
            GoalUserInfo.HEIGHT -> currentUserInfo.value = currentUserInfo.value.copy(height = value)
            GoalUserInfo.WEIGHT -> currentUserInfo.value = currentUserInfo.value.copy(weight = value)
            GoalUserInfo.STEPS -> currentGoalPreferences.value = currentGoalPreferences.value.copy(stepGoal = value)
            GoalUserInfo.WATER -> currentGoalPreferences.value = currentGoalPreferences.value.copy(waterGlassGoal = value)
            GoalUserInfo.EXERCISE -> currentGoalPreferences.value = currentGoalPreferences.value.copy(exerciseGoal = value)
            GoalUserInfo.SLEEP -> currentGoalPreferences.value = currentGoalPreferences.value.copy(sleepGoal = value)
            GoalUserInfo.DEFAULT -> Unit
        }
    }

    override suspend fun saveUserState(userStateInfo: UserStateInfo) {
        currentUserState.value = userStateInfo
    }
}