package com.apptimistiq.android.fitstreak.main.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database class for the FitStreak application.
 *
 * This class serves as the main access point for the underlying SQLite database.
 * It defines the database configuration and serves as the app's main access point
 * to the persisted data.
 *
 * @property version Database version, incremented when schema changes
 * @property entities List of entity classes that represent database tables
 * @property exportSchema Whether to export the schema to a folder for version control
 */
@Database(
    entities = [Activity::class],
    version = 1,
    exportSchema = false
)
abstract class ActivityDatabase : RoomDatabase() {

    /**
     * Provides access to the Activity Data Access Object (DAO).
     *
     * @return [ActivityDao] instance for database operations on Activity entities
     */
    abstract fun getActivityDao(): ActivityDao
}
