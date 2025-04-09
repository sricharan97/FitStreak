package com.apptimistiq.android.fitstreak.main.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow


/**
 * Data Access object for ActivityStore table
 */

@Dao
interface ActivityDao {

    /**
     * Get all the activities stored from day1
     */
    @Query("SELECT * FROM activity_store")
    fun getAllActivities(): Flow<List<Activity>>


    /**
     * Get the activity details for today
     */
    @Query("SELECT * FROM activity_store WHERE date(date_of_activity,'unixepoch','localtime') = date('now','localtime') LIMIT 1 ")
    fun getTodayActivity(): Flow<Activity>

    /**
     * Get the activities for past week
     */
    @Query(
        "SELECT * FROM activity_store WHERE date(date_of_activity,'unixepoch','localtime') BETWEEN date('now','localtime','-7 days') AND " +
                "date('now','localtime') order by date_of_activity Asc"
    )
    fun getWeekActivities(): Flow<List<Activity>>

    /**
     * Insert the activity for the day
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActivity(activity: Activity)

    /**
     * Update the activity details for the day
     */
    @Query("UPDATE activity_store SET water_glasses = :waterGlasses, sleep_hrs = :sleepHours, " +
            "exercise_cal = :exerciseCalories, steps = :steps " +
            "WHERE date(date_of_activity,'unixepoch','localtime') = date(:date,'unixepoch','localtime')")
    suspend fun updateActivityByDate(
        waterGlasses: Int,
        sleepHours: Int,
        exerciseCalories: Int,
        steps: Int,
        date: Long
    )

    /**
     * Checks if an activity entry exists for the specified date.
     * Returns true if an activity record is found for the given date, false otherwise.
     *
     * @param date The timestamp (in milliseconds) of the date to check
     * @return Boolean indicating whether an activity exists for that date
     */
    @Query("SELECT EXISTS(SELECT 1 FROM activity_store WHERE date(date_of_activity,'unixepoch','localtime') = date(:date,'unixepoch','localtime'))")
    suspend fun activityExistsForDate(date: Long): Boolean

}