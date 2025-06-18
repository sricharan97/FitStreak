package com.apptimistiq.android.fitstreak.main.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the activity_store table.
 * 
 * This interface defines methods to interact with the fitness activity data stored in the database.
 * It provides operations for querying, inserting and updating user activities such as water intake,
 * sleep hours, exercise calories burned, and step count.
 * 
 * All query methods return Flow objects to support reactive UI updates when the underlying
 * data changes.
 */
@Dao
interface ActivityDao {

    /**
     * Retrieves all activity records stored in the database.
     *
     * @return A Flow emitting a list of all activity records ordered by date
     */
    @Query("SELECT * FROM activity_store")
    fun getAllActivities(): Flow<List<Activity>>

    /**
     * Retrieves the activity record for the current day.
     * Returns only one record as there should be only one entry per day.
     *
     * @return A Flow emitting the activity for today or null if no record exists
     */
    @Query("SELECT * FROM activity_store WHERE date(date_of_activity,'unixepoch','localtime') = date('now','localtime') LIMIT 1 ")
    fun getTodayActivity(): Flow<Activity>

    /**
     * Retrieves activity records for the past 7 days including today.
     * Results are ordered by date in ascending order.
     *
     * @return A Flow emitting a list of activity records from the past week
     */
    @Query(
        "SELECT * FROM activity_store WHERE date(date_of_activity,'unixepoch','localtime') BETWEEN date('now','localtime','-6 days') AND " +
                "date('now','localtime') order by date_of_activity Asc"
    )
    fun getWeekActivities(): Flow<List<Activity>>

    /**
     * Inserts a new activity record or replaces an existing one with the same date.
     *
     * @param activity The activity object to be saved in the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActivity(activity: Activity)

    /**
     * Updates the activity metrics for a specific date.
     * This allows modifying individual activity parameters without creating a new object.
     *
     * @param waterGlasses Number of water glasses consumed
     * @param sleepHours Number of hours slept
     * @param exerciseCalories Calories burned during exercise
     * @param steps Number of steps taken
     * @param date Timestamp representing the date to update
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
     *
     * @param date Timestamp (in milliseconds) of the date to check
     * @return Boolean indicating whether an activity exists for that date
     */
    @Query("SELECT EXISTS(SELECT 1 FROM activity_store WHERE date(date_of_activity,'unixepoch','localtime') = date(:date,'unixepoch','localtime'))")
    suspend fun activityExistsForDate(date: Long): Boolean
}
