package com.apptimistiq.android.fitstreak.authentication.di

import com.apptimistiq.android.fitstreak.authentication.onboarding.GoalSelectionFragment
import dagger.Subcomponent

@Subcomponent(modules = [AuthenticationModule::class])
interface GoalSelectionComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): GoalSelectionComponent
    }

    fun inject(fragment: GoalSelectionFragment)
}