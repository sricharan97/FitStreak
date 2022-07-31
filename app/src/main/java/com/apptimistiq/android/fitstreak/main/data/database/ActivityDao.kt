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
    @Query("SELECT * FROM activity_store WHERE date(date_of_activity) = date('now') ")
    fun getTodayActivity(): Flow<Activity>

    /**
     * Get the activities for past week
     */
    @Query(
        "SELECT * FROM activity_store WHERE date(date_of_activity) BETWEEN date('now','-7 days') AND " +
                "date('now') order by date_of_activity Asc"
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
    @Update
    suspend fun updateActivity(activity: Activity)

}