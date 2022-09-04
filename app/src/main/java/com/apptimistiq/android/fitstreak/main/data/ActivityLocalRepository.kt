package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.database.ActivityDao
import com.apptimistiq.android.fitstreak.main.data.database.asDomainModel
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.asDatabaseModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
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


    override fun getTodayActivity(): Flow<List<ActivityItemUiState>> {

        //make the call main safe by switching the execution to ioDispatcher using
        //flowOn operator and conflate function to make sure that buffer has only the last value
        return activityDao.getTodayActivity().combine(goalDataSource.goalPreferences)
        { activityToday: Activity, goalPreferences: GoalPreferences ->
            activityToday.asDomainModel(goalPreferences)
        }.flowOn(ioDispatcher)
            .conflate()

    }

    override fun getWeekActivities(): Flow<List<Activity>> {

        //make the call main safe by switching the execution to ioDispatcher using
        //flowOn operator and conflate function to make sure that buffer has only the last value
        return activityDao.getWeekActivities().flowOn(ioDispatcher).conflate()
    }


    override suspend fun saveActivity(activityItems: List<ActivityItemUiState>, date: Long) =
        withContext(ioDispatcher) {
            activityDao.saveActivity(activityItems.asDatabaseModel(date))
        }

    override suspend fun updateActivity(activityItems: List<ActivityItemUiState>, date: Long) =
        withContext(ioDispatcher) {
            activityDao.updateActivity(activityItems.asDatabaseModel(date))
        }

}