package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.database.ActivityDao
import com.apptimistiq.android.fitstreak.main.data.database.asDomainModel
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ActivityLocalRepository(
    private val activityDao: ActivityDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ActivityDataSource {


    override fun getTodayActivity(): Flow<List<ActivityItemUiState>> {
        return activityDao.getTodayActivity().map {
            it.asDomainModel()
        }
    }

    override fun getWeekActivities(): Flow<List<Activity>> {
        return activityDao.getWeekActivities()
    }


    override suspend fun saveActivity(activity: Activity) = withContext(ioDispatcher) {
        activityDao.saveActivity(activity)
    }

    override suspend fun updateActivity(activity: Activity) = withContext(ioDispatcher) {
        activityDao.updateActivity(activity)
    }

}