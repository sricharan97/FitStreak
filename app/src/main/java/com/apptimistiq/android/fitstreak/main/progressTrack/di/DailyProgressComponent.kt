package com.apptimistiq.android.fitstreak.main.progressTrack.di

import com.apptimistiq.android.fitstreak.main.progressTrack.DailyProgressFragment
import dagger.Subcomponent

@Subcomponent(modules = [DailyProgressModule::class])
interface DailyProgressComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): DailyProgressComponent
    }

    fun inject(fragment: DailyProgressFragment)
}