package com.apptimistiq.android.fitstreak.main.diMain

import com.apptimistiq.android.fitstreak.main.MainActivity
import com.apptimistiq.android.fitstreak.main.home.di.HomeTransitionModule
import dagger.Subcomponent

@Subcomponent(modules = [HomeTransitionModule::class])
interface MainActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainActivityComponent
    }


    fun inject(mainActivity: MainActivity)
}