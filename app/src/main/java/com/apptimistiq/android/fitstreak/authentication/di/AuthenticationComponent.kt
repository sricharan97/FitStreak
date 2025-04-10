package com.apptimistiq.android.fitstreak.authentication.di

import com.apptimistiq.android.fitstreak.authentication.onboarding.GoalSelectionFragment
import dagger.Subcomponent

/**
 * Dagger subcomponent that provides dependencies for the authentication flow.
 * This component extends the object graph with authentication-specific dependencies
 * defined in [AuthenticationModule].
 */
@Subcomponent(modules = [AuthenticationModule::class])
interface AuthenticationComponent {

    /**
     * Factory interface for creating instances of [AuthenticationComponent].
     * Used by Dagger to instantiate the component with required dependencies.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates a new instance of [AuthenticationComponent].
         *
         * @return A new instance of AuthenticationComponent
         */
        fun create(): AuthenticationComponent
    }

    /**
     * Injects dependencies into the specified [GoalSelectionFragment].
     *
     * @param fragment The fragment to inject dependencies into
     */
    fun inject(fragment: GoalSelectionFragment)
}
