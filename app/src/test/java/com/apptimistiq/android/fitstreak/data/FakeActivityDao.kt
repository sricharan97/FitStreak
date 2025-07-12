package com.apptimistiq.android.fitstreak.main.data.test

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.database.ActivityDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FakeActivityDao : ActivityDao {
    // Existing test data
    private val activitiesStateFlow = MutableStateFlow<List<Activity>>(emptyList())
    private var currentTimeMillis = System.currentTimeMillis()

    // Error simulation flags
    private var shouldThrowException = false
    private var shouldThrowExceptionOnUpdate = false
    private var returnsNullForNoActivity = false

    // Methods to configure test data
    fun setActivities(activities: List<Activity>) {
        activitiesStateFlow.value = activities
    }

    fun setCurrentTimeMillis(timeMillis: Long) {
        currentTimeMillis = timeMillis
    }

    fun addActivity(activity: Activity) {
        val newList = activitiesStateFlow.value.toMutableList()
        newList.add(activity)
        activitiesStateFlow.value = newList
    }

    fun clearActivities() {
        activitiesStateFlow.value = emptyList()
    }

    // Error simulation methods
    fun setShouldThrowException(value: Boolean) {
        shouldThrowException = value
    }

    fun setShouldThrowExceptionOnUpdate(value: Boolean) {
        shouldThrowExceptionOnUpdate = value
    }

    fun setReturnsNullForNoActivity(value: Boolean) {
        returnsNullForNoActivity = value
    }

    // ActivityDao implementation
    override fun getAllActivities(): Flow<List<Activity>> {
        return activitiesStateFlow.map {
            if (shouldThrowException) {
                throw Exception("Test exception in getAllActivities")
            }
            it
        }
    }

    override fun getTodayActivity(): Flow<Activity> {
        return activitiesStateFlow.map { activities ->
            if (shouldThrowException) {
                throw Exception("Test exception in getTodayActivity")
            }

            val activity = activities.find { isSameDay(it.dateOfActivity, currentTimeMillis) }

            if (activity == null) {
                if (returnsNullForNoActivity) {
                    throw NullPointerException("No activity found for today")
                }
                Activity(0, currentTimeMillis, 0, 0, 0, 0)
            } else {
                activity
            }
        }
    }

    override fun getWeekActivities(): Flow<List<Activity>> {
        return activitiesStateFlow.map { activities ->
            if (shouldThrowException) {
                throw Exception("Test exception in getWeekActivities")
            }
            activities.filter {
                it.dateOfActivity >= currentTimeMillis - 7 * 24 * 60 * 60 * 1000
            }.sortedBy { it.dateOfActivity }
        }
    }

    override suspend fun saveActivity(activity: Activity) {
        if (shouldThrowException) {
            throw Exception("Test exception in saveActivity")
        }

        val id = if (activity.id == 0L) System.currentTimeMillis() else activity.id
        val updatedActivity = activity.copy(id = id)
        val activities = activitiesStateFlow.value.toMutableList()
        val index = activities.indexOfFirst { it.id == id }

        if (index >= 0) {
            activities[index] = updatedActivity
        } else {
            activities.add(updatedActivity)
        }

        activitiesStateFlow.value = activities
    }

    override suspend fun updateActivityByDate(
        waterGlasses: Int,
        sleepHours: Int,
        exerciseCalories: Int,
        steps: Int,
        date: Long
    ) {
        if (shouldThrowExceptionOnUpdate || shouldThrowException) {
            throw Exception("Test exception in updateActivityByDate")
        }

        val activities = activitiesStateFlow.value.toMutableList()
        val index = activities.indexOfFirst { isSameDay(it.dateOfActivity, date) }

        if (index >= 0) {
            activities[index] = activities[index].copy(
                waterGlasses = waterGlasses,
                sleepHours = sleepHours,
                exerciseCalories = exerciseCalories,
                steps = steps
            )
            activitiesStateFlow.value = activities
        }
    }

    override suspend fun activityExistsForDate(date: Long): Boolean {
        if (shouldThrowException) {
            throw Exception("Test exception in activityExistsForDate")
        }
        return activitiesStateFlow.value.any { isSameDay(it.dateOfActivity, date) }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}