package com.apptimistiq.android.fitstreak.di

import android.content.Context
import com.apptimistiq.android.fitstreak.authentication.di.AuthenticationComponent
import com.apptimistiq.android.fitstreak.authentication.di.LoginComponent
import com.apptimistiq.android.fitstreak.main.dashboard.di.DashboardComponent
import com.apptimistiq.android.fitstreak.main.dashboard.di.GoalEditComponent
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.RecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.UserProfileDataSource
import com.apptimistiq.android.fitstreak.main.diMain.MainActivityComponent
import com.apptimistiq.android.fitstreak.main.home.di.HomeTransitionComponent
import com.apptimistiq.android.fitstreak.main.progressTrack.di.DailyProgressComponent
import com.apptimistiq.android.fitstreak.main.progressTrack.di.EditActivityComponent
import com.apptimistiq.android.fitstreak.main.recipe.di.RecipesTrackComponent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

/**
 * Root Dagger component for the application.
 *
 * The AppComponent provides application-wide singletons and serves as the parent
 * component for all feature-specific subcomponents. It's responsible for providing
 * dependencies that need to be shared across the entire application.
 *
 * This component lives throughout the application lifecycle and is initialized
 * during application startup.
 */
@Singleton
@Component(
    modules = [
        AppModule::class,
        DatabaseModule::class,
        NetworkModule::class,
        ViewModelBuilderModule::class,
        SubComponentsModule::class
    ]
)
interface AppComponent {

    /**
     * Factory interface for creating instances of the AppComponent.
     * 
     * Using this factory pattern allows for proper injection of dependencies
     * during the component's construction.
     */
    @Component.Factory
    interface Factory {
        /**
         * Creates a new instance of AppComponent.
         *
         * @param applicationContext The application context to be available in the dependency graph
         * @return An instance of AppComponent
         */
        fun create(@BindsInstance applicationContext: Context): AppComponent
    }
    
    // Factory methods for feature-specific subcomponents
    
    /**
     * Returns a factory for creating DailyProgressComponent instances.
     */
    fun dailyProgressComponent(): DailyProgressComponent.Factory
    
    /**
     * Returns a factory for creating AuthenticationComponent instances.
     */
    fun authenticationComponent(): AuthenticationComponent.Factory
    
    /**
     * Returns a factory for creating RecipesTrackComponent instances.
     */
    fun recipesTrackComponent(): RecipesTrackComponent.Factory
    
    /**
     * Returns a factory for creating DashboardComponent instances.
     */
    fun DashboardComponent(): DashboardComponent.Factory
    
    /**
     * Returns a factory for creating GoalEditComponent instances.
     */
    fun GoalEditComponent(): GoalEditComponent.Factory
    
    /**
     * Returns a factory for creating EditActivityComponent instances.
     */
    fun EditActivityComponent(): EditActivityComponent.Factory
    
    /**
     * Returns a factory for creating LoginComponent instances.
     */
    fun loginComponent(): LoginComponent.Factory
    
    /**
     * Returns a factory for creating HomeTransitionComponent instances.
     */
    fun homeTransitionComponent(): HomeTransitionComponent.Factory
    
    /**
     * Returns a factory for creating MainActivityComponent instances.
     */
    fun mainActivityComponent(): MainActivityComponent.Factory

    // Application-wide data sources provided by this component
    
    /**
     * Provides access to activity-related data operations.
     */
    val activityDataSource: ActivityDataSource
    
    /**
     * Provides access to user profile data operations.
     */
    val userProfileDataSource: UserProfileDataSource
    
    /**
     * Provides access to recipe data operations from remote sources.
     */
    val recipeRemoteDataSource: RecipeRemoteDataSource
}

/**
 * Module that declares all subcomponents available in the application.
 *
 * This module ensures that all feature-specific subcomponents are registered with
 * the Dagger graph and can be properly instantiated through the AppComponent.
 */
@Module(
    subcomponents = [
        DailyProgressComponent::class,
        AuthenticationComponent::class,
        RecipesTrackComponent::class, 
        DashboardComponent::class, 
        GoalEditComponent::class,
        EditActivityComponent::class, 
        LoginComponent::class, 
        HomeTransitionComponent::class,
        MainActivityComponent::class
    ]
)
object SubComponentsModule
