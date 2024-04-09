package com.apptimistiq.android.fitstreak.authentication.di

import com.apptimistiq.android.fitstreak.authentication.onboarding.LoginFragment
import dagger.Subcomponent

@Subcomponent(modules = [AuthenticationModule::class])
interface AuthenticationComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): AuthenticationComponent
    }

    fun inject(fragment: LoginFragment)
}