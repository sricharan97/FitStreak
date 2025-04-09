package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.GoalType
import com.apptimistiq.android.fitstreak.main.dashboard.GoalUserInfo
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

// @Inject tells Dagger how to provide instances of this type
@Singleton
class ActivityLocalRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val userProfileDataSource: UserProfileDataSource
) : ActivityDataSource {


    override fun getTodayActivity(): Flow<List<ActivityItemUiState>?> {

        //make the call main safe by switching the execution to ioDispatcher using
        //flowOn operator and conflate function to make sure that buffer has only the last value
        return activityDao.getTodayActivity().combine(userProfileDataSource.goalPreferences)
        { activityToday: Activity?, goalPreferences: GoalPreferences ->
            activityToday?.asDomainModel(goalPreferences)
        }.flowOn(ioDispatcher)
            .conflate()

    }

    override fun getWeekActivities(): Flow<List<Activity>> {

        //make the call main safe by switching the execution to ioDispatcher using
        //flowOn operator and conflate function to make sure that buffer has only the last value
        return activityDao.getWeekActivities().flowOn(ioDispatcher)
    }

    override fun getCurrentActivityVal(activityType: ActivityType): Flow<Int> {
        return activityDao.getTodayActivity().map { it.asActivityTypeVal(activityType) }
    }

    override fun getCurrentUserState(): Flow<UserStateInfo> {
        return userProfileDataSource.userStateInfo.flowOn(ioDispatcher)
    }

    override fun getCurrentGoals(): Flow<GoalPreferences> {
        return userProfileDataSource.goalPreferences.flowOn(ioDispatcher)
    }

    override fun getCurrentUserInfo(): Flow<UserInfoPreferences> {
        return userProfileDataSource.userInfoPreferences.flowOn(ioDispatcher)
    }

    override fun getCurrentGoalUserInfo(goalUserInfo: GoalUserInfo): Flow<Int> {
        when (goalUserInfo) {
            GoalUserInfo.WEIGHT -> {
                return userProfileDataSource.weightInfo.flowOn(ioDispatcher)
            }
            GoalUserInfo.HEIGHT -> {
                return userProfileDataSource.heightInfo.flowOn(ioDispatcher)
            }
            GoalUserInfo.WATER -> {
                return userProfileDataSource.waterGoal.flowOn(ioDispatcher)
            }
            GoalUserInfo.EXERCISE -> {
                return userProfileDataSource.exerciseGoal.flowOn(ioDispatcher)
            }
            GoalUserInfo.STEPS -> {
                return userProfileDataSource.stepsGoal.flowOn(ioDispatcher)
            }
            GoalUserInfo.SLEEP -> {
                return userProfileDataSource.sleepGoal.flowOn(ioDispatcher)
            }

            GoalUserInfo.DEFAULT -> {
                return flow {
                    emit(0)
                }.flowOn(ioDispatcher)
            }

        }
    }

    override suspend fun saveActivity(activityItems: List<ActivityItemUiState>, date: Long) =
        withContext(ioDispatcher) {
            activityDao.saveActivity(activityItems.asDatabaseModel(date))
        }

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
                saveActivity(activityItems, date)            }
        }

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

    override suspend fun saveGoalInfo(goalInfoType: GoalUserInfo, value: Int) {
        withContext(ioDispatcher) {
            when (goalInfoType) {
                GoalUserInfo.STEPS -> userProfileDataSource.updateStepGoal(value)
                GoalUserInfo.EXERCISE -> userProfileDataSource.updateExerciseGoal(value)
                GoalUserInfo.SLEEP -> userProfileDataSource.updateSleepGoal(value)
                GoalUserInfo.WATER -> userProfileDataSource.updateWaterGlassesGoal(value)
                GoalUserInfo.WEIGHT -> userProfileDataSource.updateUserWeight(value)
                GoalUserInfo.HEIGHT -> userProfileDataSource.updateUserHeight(value)
                GoalUserInfo.DEFAULT -> {}
            }
        }
    }

    override suspend fun saveUserState(userStateInfo: UserStateInfo) {
        withContext(ioDispatcher) {
            userProfileDataSource.updateUserStateInfo(userStateInfo)
        }
    }
}