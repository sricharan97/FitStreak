package com.apptimistiq.android.fitstreak.di

import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.ActivityLocalRepository
import com.apptimistiq.android.fitstreak.main.data.GoalDataSource
import com.apptimistiq.android.fitstreak.main.data.GoalsRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton


// Tells Dagger this is a Dagger module
// Because of @Binds, AppModule needs to be an abstract class
@Module
abstract class AppModule {

    // Makes Dagger provide ActivityLocalRepository when a ActivityDataSource type is requested
    @Singleton
    @Binds
    abstract fun bindActivityDataSource(activityLocalRepository: ActivityLocalRepository): ActivityDataSource

    //Makes Dagger provide GoalsRepository when a GoalDataSource is requested
    @Singleton
    @Binds
    abstract fun bindGoalDataSource(goalRepository: GoalsRepository): GoalDataSource

}