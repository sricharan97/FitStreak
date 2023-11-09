package com.apptimistiq.android.fitstreak.main.dashboard.di

import com.apptimistiq.android.fitstreak.main.dashboard.DashboardFragment
import dagger.Subcomponent


@Subcomponent(modules = [DashboardModule::class])
interface DashboardComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): DashboardComponent
    }

    fun inject(fragment: DashboardFragment)
}