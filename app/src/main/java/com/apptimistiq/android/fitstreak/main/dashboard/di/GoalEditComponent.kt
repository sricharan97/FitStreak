package com.apptimistiq.android.fitstreak.main.dashboard.di

import com.apptimistiq.android.fitstreak.main.dashboard.GoalEditFragment
import dagger.Subcomponent


@Subcomponent(modules = [DashboardModule::class])
interface GoalEditComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): GoalEditComponent
    }

    fun inject(fragment: GoalEditFragment)
}