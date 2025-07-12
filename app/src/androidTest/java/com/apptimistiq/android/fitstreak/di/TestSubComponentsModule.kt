package com.apptimistiq.android.fitstreak.di

import com.apptimistiq.android.fitstreak.authentication.di.AuthenticationComponent
import com.apptimistiq.android.fitstreak.authentication.di.LoginComponent
import com.apptimistiq.android.fitstreak.main.dashboard.di.DashboardComponent
import com.apptimistiq.android.fitstreak.main.dashboard.di.GoalEditComponent
import com.apptimistiq.android.fitstreak.main.diMain.MainActivityComponent
import com.apptimistiq.android.fitstreak.main.home.di.HomeTransitionComponent
import com.apptimistiq.android.fitstreak.main.progressTrack.di.EditActivityComponent
import com.apptimistiq.android.fitstreak.main.recipe.di.RecipesTrackComponent
import dagger.Module

@Module(
    subcomponents = [
        AuthenticationComponent::class,
        RecipesTrackComponent::class,
        DashboardComponent::class,
        GoalEditComponent::class,
        LoginComponent::class,
        HomeTransitionComponent::class,
        MainActivityComponent::class
        // DailyProgressComponent is intentionally excluded to avoid conflicts
    ]
)
object TestSubComponentsModule