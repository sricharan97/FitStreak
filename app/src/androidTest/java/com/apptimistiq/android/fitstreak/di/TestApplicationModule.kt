package com.apptimistiq.android.fitstreak.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.AuthDataSource
import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.data.FakeAuthDataSource
import com.apptimistiq.android.fitstreak.data.FakeRecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.FakePreferencesDataStore
import com.apptimistiq.android.fitstreak.main.data.RecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.UserProfileDataSource
import com.apptimistiq.android.fitstreak.main.data.database.ActivityDao
import com.apptimistiq.android.fitstreak.main.data.test.FakeActivityDao
import com.apptimistiq.android.fitstreak.main.data.test.FakeUserProfileDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TestApplicationModule {

    @Provides
    @Singleton
    fun provideActivityDataSource(): ActivityDataSource {
        return FakeActivityDataSource()
    }

    @Provides
    @Singleton
    fun provideAuthDataSource(): AuthDataSource {
        return FakeAuthDataSource()
    }

    @Provides
    @Singleton
    fun provideUserProfileDataSource() : UserProfileDataSource{
        return FakeUserProfileDataSource()
    }

    @Provides
    @Singleton
    fun provideRecipeRemoteDataSource() : RecipeRemoteDataSource{
        return FakeRecipeRemoteDataSource()
    }

    @Provides
    @Singleton
    fun provideDataStorePreference() : DataStore<Preferences> {
        return FakePreferencesDataStore()
    }

    @Provides
    @Singleton
    fun provideActivityDao(): ActivityDao {
        return FakeActivityDao()
    }


}
