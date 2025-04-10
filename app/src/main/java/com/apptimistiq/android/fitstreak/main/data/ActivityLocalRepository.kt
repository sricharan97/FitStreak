package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.GoalType
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.database.ActivityDao
import com.apptimistiq.android.fitstreak.main.data.mappers.asActivityTypeVal
import com.apptimistiq.android.fitstreak.main.data.mappers.asDomainModel
import com.apptimistiq.android.fitstreak.main.data.domain.*
import com.apptimistiq.android.fitstreak.main.data.mappers.asDatabaseModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for handling fitness activity data.
 * 
 * This class serves as a single source of truth for activity data in the application
 * by managing local database operations through the DAO and coordinating with the
 * user profile data source for preferences. It implements the [ActivityDataSource]
 * interface to provide a clean API for the rest of the application.
 * 
 * @property activityDao Data Access Object for performing database operations
 * @property ioDispatcher Coroutine dispatcher for IO operations
 * @property userProfileDataSource Data source for user profile and preferences
 */
@Singleton
class ActivityLocalRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val userProfileDataSource: UserProfileDataSource
) : ActivityDataSource {

    //region Activity Data Operations
    
    /**
     * Retrieves today's activity combined with user goal preferences.
     * 
     * @return Flow emitting a list of [ActivityItemUiState] for today's activities
     */
    override fun getTodayActivity(): Flow<List<ActivityItemUiState>?> {
        return activityDao.getTodayActivity().combine(userProfileDataSource.goalPreferences)
        { activityToday: Activity?, goalPreferences: GoalPreferences ->
            activityToday?.asDomainModel(goalPreferences)
        }.flowOn(ioDispatcher)
            .conflate()
    }

    /**
     * Retrieves activities for the current week.
     * 
     * @return Flow emitting a list of [Activity] for the current week
     */
    override fun getWeekActivities(): Flow<List<Activity>> {
        return activityDao.getWeekActivities().flowOn(ioDispatcher)
    }

    /**
     * Gets the current value for a specific activity type.
     * 
     * @param activityType The type of activity to query
     * @return Flow emitting the current value as integer
     */
    override fun getCurrentActivityVal(activityType: ActivityType): Flow<Int> {
        return activityDao.getTodayActivity().map { it.asActivityTypeVal(activityType) }
    }

    /**
     * Saves a list of activity items for a specific date.
     * 
     * @param activityItems List of activity items to save
     * @param date Timestamp representing the date for the activities
     */
    override suspend fun saveActivity(activityItems: List<ActivityItemUiState>, date: Long) =
        withContext(ioDispatcher) {
            activityDao.saveActivity(activityItems.asDatabaseModel(date))
        }

    /**
     * Updates existing activities for a specific date or creates new ones if none exist.
     * 
     * @param activityItems List of activity items to update
     * @param date Timestamp representing the date for the activities
     */
    override suspend fun updateActivity(activityItems: List<ActivityItemUiState>, date: Long) =
        withContext(ioDispatcher) {
            // Check if an activity exists for this date
            val exists = activityDao.activityExistsForDate(date)
            if(exists) {
                // Extract values from activity items
                val waterGlasses = activityItems.find { it.dataType == ActivityType.WATER }?.currentReading ?: 0
                val sleepHours = activityItems.find { it.dataType == ActivityType.SLEEP }?.currentReading ?: 0
                val exerciseCalories = activityItems.find { it.dataType == ActivityType.EXERCISE }?.currentReading ?: 0
                val steps = activityItems.find { it.dataType == ActivityType.STEP }?.currentReading ?: 0

                // Update using the new method name
                activityDao.updateActivityByDate(waterGlasses, sleepHours, exerciseCalories, steps, date)
            }
            else{
                // If the activity doesn't exist, save it as a new entry
                saveActivity(activityItems, date)
            }
        }
    //endregion

    //region User Preferences Operations
    
    /**
     * Retrieves current user state information.
     * 
     * @return Flow emitting [UserStateInfo]
     */
    override fun getCurrentUserState(): Flow<UserStateInfo> {
        return userProfileDataSource.userStateInfo.flowOn(ioDispatcher)
    }

    /**
     * Retrieves current goal preferences.
     * 
     * @return Flow emitting [GoalPreferences]
     */
    override fun getCurrentGoals(): Flow<GoalPreferences> {
        return userProfileDataSource.goalPreferences.flowOn(ioDispatcher)
    }

    /**
     * Retrieves current user information preferences.
     * 
     * @return Flow emitting [UserInfoPreferences]
     */
    override fun getCurrentUserInfo(): Flow<UserInfoPreferences> {
        return userProfileDataSource.userInfoPreferences.flowOn(ioDispatcher)
    }

    /**
     * Retrieves a specific goal or user information value.
     * 
     * @param goalUserInfo The type of goal or user info to retrieve
     * @return Flow emitting an integer value for the requested info
     */
    override fun getCurrentGoalUserInfo(goalUserInfo: GoalUserInfo): Flow<Int> {
        return when (goalUserInfo) {
            GoalUserInfo.WEIGHT -> userProfileDataSource.weightInfo.flowOn(ioDispatcher)
            GoalUserInfo.HEIGHT -> userProfileDataSource.heightInfo.flowOn(ioDispatcher)
            GoalUserInfo.WATER -> userProfileDataSource.waterGoal.flowOn(ioDispatcher)
            GoalUserInfo.EXERCISE -> userProfileDataSource.exerciseGoal.flowOn(ioDispatcher)
            GoalUserInfo.STEPS -> userProfileDataSource.stepsGoal.flowOn(ioDispatcher)
            GoalUserInfo.SLEEP -> userProfileDataSource.sleepGoal.flowOn(ioDispatcher)
            GoalUserInfo.DEFAULT -> flow { emit(0) }.flowOn(ioDispatcher)
        }
    }

    /**
     * Updates a specific fitness goal.
     * 
     * @param goalType The type of goal to update
     * @param value The new goal value
     */
    override suspend fun saveGoal(goalType: GoalType, value: Int) {
        withContext(ioDispatcher) {
            when (goalType) {
                GoalType.STEP -> userProfileDataSource.updateStepGoal(value)
                GoalType.EXERCISE -> userProfileDataSource.updateExerciseGoal(value)
                GoalType.SLEEP -> userProfileDataSource.updateSleepGoal(value)
                GoalType.WATER -> userProfileDataSource.updateWaterGlassesGoal(value)
            }
        }
    }

    /**
     * Updates a specific goal or user information value.
     * 
     * @param goalInfoType The type of goal or user info to update
     * @param value The new value
     */
    override suspend fun saveGoalInfo(goalInfoType: GoalUserInfo, value: Int) {
        withContext(ioDispatcher) {
            when (goalInfoType) {
                GoalUserInfo.STEPS -> userProfileDataSource.updateStepGoal(value)
                GoalUserInfo.EXERCISE -> userProfileDataSource.updateExerciseGoal(value)
                GoalUserInfo.SLEEP -> userProfileDataSource.updateSleepGoal(value)
                GoalUserInfo.WATER -> userProfileDataSource.updateWaterGlassesGoal(value)
                GoalUserInfo.WEIGHT -> userProfileDataSource.updateUserWeight(value)
                GoalUserInfo.HEIGHT -> userProfileDataSource.updateUserHeight(value)
                GoalUserInfo.DEFAULT -> {} // No action needed for default
            }
        }
    }

    /**
     * Updates the user state information.
     * 
     * @param userStateInfo The new user state to save
     */
    override suspend fun saveUserState(userStateInfo: UserStateInfo) {
        withContext(ioDispatcher) {
            userProfileDataSource.updateUserStateInfo(userStateInfo)
        }
    }
    //endregion
}
