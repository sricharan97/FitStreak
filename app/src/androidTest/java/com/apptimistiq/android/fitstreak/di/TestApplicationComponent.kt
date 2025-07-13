package com.apptimistiq.android.fitstreak.di

import com.apptimistiq.android.fitstreak.authentication.onboarding.GoalSelectionFragmentTest
import com.apptimistiq.android.fitstreak.authentication.onboarding.LoginFragmentTest
import com.apptimistiq.android.fitstreak.main.dashboard.DashboardFragmentTest
import com.apptimistiq.android.fitstreak.main.dashboard.GoalEditFragmentTest
import com.apptimistiq.android.fitstreak.main.progressTrack.DailyProgressFragmentTest
import com.apptimistiq.android.fitstreak.main.progressTrack.EditActivityFragmentTest
import com.apptimistiq.android.fitstreak.main.recipe.RecipesFragmentTest
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        TestApplicationModule::class,
        ViewModelBuilderModule::class,
        SubComponentsModule::class
    ]
)
interface TestApplicationComponent : AppComponent {
    fun inject(test: GoalSelectionFragmentTest)
    fun inject(test: LoginFragmentTest)
    fun inject(test: RecipesFragmentTest)
    fun inject(test: DashboardFragmentTest)
    fun inject(test: GoalEditFragmentTest)
    fun inject(test: EditActivityFragmentTest)
    fun inject(test: DailyProgressFragmentTest)
}
