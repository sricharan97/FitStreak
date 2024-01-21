package com.apptimistiq.android.fitstreak.di

import android.content.Context
import com.apptimistiq.android.fitstreak.authentication.di.AuthenticationComponent
import com.apptimistiq.android.fitstreak.main.dashboard.di.DashboardComponent
import com.apptimistiq.android.fitstreak.main.dashboard.di.GoalEditComponent
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.RecipeRemoteDataSource
import com.apptimistiq.android.fitstreak.main.data.UserProfileDataSource
import com.apptimistiq.android.fitstreak.main.progressTrack.di.DailyProgressComponent
import com.apptimistiq.android.fitstreak.main.progressTrack.di.EditActivityComponent
import com.apptimistiq.android.fitstreak.main.recipe.di.RecipesTrackComponent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

//Definition of a Dagger component
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

    // Factory to create instances of the AppComponent
    @Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance applicationContext: Context): AppComponent
    }

    fun dailyProgressComponent(): DailyProgressComponent.Factory
    fun authenticationComponent(): AuthenticationComponent.Factory
    fun recipesTrackComponent(): RecipesTrackComponent.Factory
    fun DashboardComponent(): DashboardComponent.Factory
    fun GoalEditComponent(): GoalEditComponent.Factory
    fun EditActivityComponent(): EditActivityComponent.Factory

    val activityDataSource: ActivityDataSource
    val userProfileDataSource: UserProfileDataSource
    val recipeRemoteDataSource: RecipeRemoteDataSource

}

@Module(
    subcomponents = [DailyProgressComponent::class, AuthenticationComponent::class,
        RecipesTrackComponent::class, DashboardComponent::class, GoalEditComponent::class,
        EditActivityComponent::class]
)
object SubComponentsModule