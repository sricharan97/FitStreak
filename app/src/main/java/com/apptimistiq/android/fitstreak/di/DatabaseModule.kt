package com.apptimistiq.android.fitstreak.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.apptimistiq.android.fitstreak.main.data.database.ActivityDao
import com.apptimistiq.android.fitstreak.main.data.database.ActivityDatabase
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Dagger module that provides database and data storage related dependencies.
 * This module contains provider methods for Room database instances, DAOs,
 * DataStore preferences, and coroutine dispatchers used across the application.
 */
@Module
object DatabaseModule {

    // Constants
    private const val USER_PROFILE_PREFERENCES_NAME = "user_profile_preferences"
    private const val DATABASE_NAME = "activity_database"

    /**
     * Provides the Room database instance for the application.
     *
     * @param context The application context used to build the database.
     * @return A singleton instance of [ActivityDatabase].
     */
    @JvmStatic
    @Singleton
    @Provides
    fun providesActivityDatabase(context: Context): ActivityDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ActivityDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the DAO for accessing and manipulating activity data.
     *
     * @param activityDatabase The database from which to obtain the DAO.
     * @return An instance of [ActivityDao].
     */
    @JvmStatic
    @Singleton
    @Provides
    fun providesActivityDao(activityDatabase: ActivityDatabase): ActivityDao {
        return activityDatabase.getActivityDao()
    }

    /**
     * Provides an IO dispatcher for performing database operations on a background thread.
     *
     * @return The IO [CoroutineDispatcher] for executing database operations.
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideDispatcherToRepository(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    /**
     * Provides a DataStore for storing user preferences.
     * Includes corruption handling and migration from SharedPreferences.
     *
     * @param context The application context used to create the DataStore.
     * @return A singleton instance of [DataStore]<[Preferences]>.
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideDataStorePreferences(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(
                SharedPreferencesMigration(
                    context.applicationContext,
                    USER_PROFILE_PREFERENCES_NAME
                )
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = {
                context.applicationContext.preferencesDataStoreFile(
                    USER_PROFILE_PREFERENCES_NAME
                )
            }
        )
    }
}
