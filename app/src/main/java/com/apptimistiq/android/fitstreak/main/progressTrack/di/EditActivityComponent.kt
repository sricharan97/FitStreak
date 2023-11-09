package com.apptimistiq.android.fitstreak.main.progressTrack.di

import com.apptimistiq.android.fitstreak.main.progressTrack.EditActivityFragment
import dagger.Subcomponent


@Subcomponent(modules = [DailyProgressModule::class])
interface EditActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): EditActivityComponent
    }

    fun inject(fragment: EditActivityFragment)
}