package com.apptimistiq.android.fitstreak.main.home.di

import com.apptimistiq.android.fitstreak.main.home.HomeTransitionFragment
import dagger.Subcomponent

@Subcomponent(modules = [HomeTransitionModule::class])
interface HomeTransitionComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): HomeTransitionComponent
    }


    fun inject(fragment: HomeTransitionFragment)
}