package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.authentication.GoalType
import com.apptimistiq.android.fitstreak.main.dashboard.GoalUserInfo
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.database.ActivityDao
import com.apptimistiq.android.fitstreak.main.data.database.asActivityTypeVal
import com.apptimistiq.android.fitstreak.main.data.database.asDomainModel
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import com.apptimistiq.android.fitstreak.main.data.domain.asDatabaseModel
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
    private val goalDataSource: GoalDataSource
) : ActivityDataSource {


    override fun getTodayActivity(): Flow<List<ActivityItemUiState>?> {

        //make the call main safe by switching the execution to ioDispatcher using
        //flowOn operator and conflate function to make sure that buffer has only the last value
        return activityDao.getTodayActivity().combine(goalDataSource.goalPreferences)
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

    override fun getCurrentGoals(): Flow<GoalPreferences> {
        return goalDataSource.goalPreferences.flowOn(ioDispatcher)
    }

    override fun getCurrentUserInfo(): Flow<UserInfoPreferences> {
        return goalDataSource.userInfoPreferences.flowOn(ioDispatcher)
    }

    override fun getCurrentGoalUserInfo(goalUserInfo: GoalUserInfo): Flow<Int> {
        when (goalUserInfo) {
            GoalUserInfo.WEIGHT -> {
                return goalDataSource.weightInfo.flowOn(ioDispatcher)
            }
            GoalUserInfo.HEIGHT -> {
                return goalDataSource.heightInfo.flowOn(ioDispatcher)
            }
            GoalUserInfo.WATER -> {
                return goalDataSource.waterGoal.flowOn(ioDispatcher)
            }
            GoalUserInfo.EXERCISE -> {
                return goalDataSource.exerciseGoal.flowOn(ioDispatcher)
            }
            GoalUserInfo.STEPS -> {
                return goalDataSource.stepsGoal.flowOn(ioDispatcher)
            }
            GoalUserInfo.SLEEP -> {
                return goalDataSource.sleepGoal.flowOn(ioDispatcher)
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
            activityDao.updateActivity(activityItems.asDatabaseModel(date))
        }

    override suspend fun saveGoal(goalType: GoalType, value: Int) {
        withContext(ioDispatcher) {
            when (goalType) {
                GoalType.STEP -> goalDataSource.updateStepGoal(value)
                GoalType.EXERCISE -> goalDataSource.updateExerciseGoal(value)
                GoalType.SLEEP -> goalDataSource.updateSleepGoal(value)
                GoalType.WATER -> goalDataSource.updateWaterGlassesGoal(value)
            }
        }
    }

    override suspend fun saveGoalInfo(goalInfoType: GoalUserInfo, value: Int) {
        withContext(ioDispatcher) {
            when (goalInfoType) {
                GoalUserInfo.STEPS -> goalDataSource.updateStepGoal(value)
                GoalUserInfo.EXERCISE -> goalDataSource.updateExerciseGoal(value)
                GoalUserInfo.SLEEP -> goalDataSource.updateSleepGoal(value)
                GoalUserInfo.WATER -> goalDataSource.updateWaterGlassesGoal(value)
                GoalUserInfo.WEIGHT -> goalDataSource.updateUserWeight(value)
                GoalUserInfo.HEIGHT -> goalDataSource.updateUserHeight(value)
                GoalUserInfo.DEFAULT -> {}
            }
        }
    }
}