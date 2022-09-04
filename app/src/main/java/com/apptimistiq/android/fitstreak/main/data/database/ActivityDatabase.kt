package com.apptimistiq.android.fitstreak.main.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Activity::class], version = 1, exportSchema = false)
abstract class ActivityDatabase : RoomDatabase() {

    abstract fun getActivityDao(): ActivityDao

}