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


private const val GOAL_PREFERENCES_NAME = "goal_preferences"


@Module
object DatabaseModule {


    @JvmStatic
    @Singleton
    @Provides
    fun providesActivityDao(activityDatabase: ActivityDatabase): ActivityDao {
        return activityDatabase.getActivityDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providesActivityDatabase(context: Context): ActivityDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ActivityDatabase::class.java,
            "activity_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    //Make Dagger provide io Dispatcher when a Coroutine dispatcher is requested
    @JvmStatic
    @Singleton
    @Provides
    fun provideDispatcherToRepository(): CoroutineDispatcher {
        return Dispatchers.IO
    }


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
                    GOAL_PREFERENCES_NAME
                )
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = {
                context.applicationContext.preferencesDataStoreFile(
                    GOAL_PREFERENCES_NAME
                )
            }
        )

    }
}