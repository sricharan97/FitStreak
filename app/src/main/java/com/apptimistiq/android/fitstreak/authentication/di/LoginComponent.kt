package com.apptimistiq.android.fitstreak.authentication.di

import com.apptimistiq.android.fitstreak.authentication.onboarding.LoginFragment
import dagger.Subcomponent

/**
 * Dagger subcomponent that provides dependencies for the login flow.
 * This component extends the object graph with login-specific dependencies
 * defined in [AuthenticationModule].
 */
@Subcomponent(modules = [AuthenticationModule::class])
interface LoginComponent {

    /**
     * Factory interface for creating instances of [LoginComponent].
     * Used by Dagger to instantiate the component with required dependencies.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates a new instance of [LoginComponent].
         *
         * @return A new instance of LoginComponent
         */
        fun create(): LoginComponent
    }

    /**
     * Injects dependencies into the specified [LoginFragment].
     *
     * @param fragment The fragment to inject dependencies into
     */
    fun inject(fragment: LoginFragment)
}
