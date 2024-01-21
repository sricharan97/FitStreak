package com.apptimistiq.android.fitstreak.di

import com.apptimistiq.android.fitstreak.main.data.*
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

    //Makes Dagger provide UserProfileRepository when a UserProfileDataSource is requested
    @Singleton
    @Binds
    abstract fun bindGoalDataSource(goalRepository: UserProfileRepository): UserProfileDataSource

    //Makes Dagger provide RecipeRemoteRepository when a RecipeRemoteDatasource is requested
    @Singleton
    @Binds
    abstract fun bindRecipeRemoteDataSource(recipeRemoteRepository: RecipeRemoteRepository): RecipeRemoteDataSource

}