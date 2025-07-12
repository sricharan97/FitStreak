package com.apptimistiq.android.fitstreak.main.home.di

import com.apptimistiq.android.fitstreak.authentication.di.AuthenticationModule
import com.apptimistiq.android.fitstreak.main.home.HomeTransitionFragment
import dagger.Subcomponent

/**
 * Dagger Subcomponent for the Home Transition feature.
 * 
 * This component extends the object graph with dependencies specifically
 * required by the Home Transition functionality. It is responsible for
 * injecting dependencies into the HomeTransitionFragment.
 */
@Subcomponent(modules = [HomeTransitionModule::class, AuthenticationModule::class])
interface HomeTransitionComponent {

    /**
     * Factory interface for creating instances of the HomeTransitionComponent.
     * 
     * This factory is used by the parent component to create new instances
     * of this subcomponent.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates a new instance of HomeTransitionComponent
         * 
         * @return HomeTransitionComponent A new instance of the component
         */
        fun create(): HomeTransitionComponent
    }

    /**
     * Injects dependencies into the HomeTransitionFragment
     * 
     * @param fragment The fragment into which dependencies will be injected
     */
    fun inject(fragment: HomeTransitionFragment)
}