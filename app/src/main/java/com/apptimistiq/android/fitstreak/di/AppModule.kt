package com.apptimistiq.android.fitstreak.di

import com.apptimistiq.android.fitstreak.main.data.*
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 * Dagger module that provides dependency bindings for data sources in the FitStreak application.
 * 
 * This module uses the @Binds annotation to tell Dagger how to satisfy dependencies by mapping
 * interface types to their concrete implementations. All dependencies are provided as singletons
 * to ensure consistent instances throughout the application lifecycle.
 */
@Module
abstract class AppModule {

    /**
     * Binds the implementation of ActivityDataSource to ActivityLocalRepository.
     * 
     * @param activityLocalRepository The concrete implementation to be used
     * @return The interface type for dependency injection
     */
    @Singleton
    @Binds
    abstract fun bindActivityDataSource(activityLocalRepository: ActivityLocalRepository): ActivityDataSource

    /**
     * Binds the implementation of UserProfileDataSource to UserProfileRepository.
     * 
     * @param goalRepository The concrete implementation to be used
     * @return The interface type for dependency injection
     */
    @Singleton
    @Binds
    abstract fun bindUserProfileDataSource(goalRepository: UserProfileRepository): UserProfileDataSource

    /**
     * Binds the implementation of AuthDataSource to FirebaseAuthRepository.
     *
     * @param firebaseAuthRepository The concrete implementation to be used
     * @return The interface type for dependency injection
     */
    @Singleton
    @Binds
    abstract fun bindAuthDataSource(firebaseAuthRepository: FirebaseAuthRepository): AuthDataSource

    /**
     * Binds the implementation of RecipeRemoteDataSource to RecipeRemoteRepository.
     * 
     * @param recipeRemoteRepository The concrete implementation to be used
     * @return The interface type for dependency injection
     */
    @Singleton
    @Binds
    abstract fun bindRecipeRemoteDataSource(recipeRemoteRepository: RecipeRemoteRepository): RecipeRemoteDataSource
}
