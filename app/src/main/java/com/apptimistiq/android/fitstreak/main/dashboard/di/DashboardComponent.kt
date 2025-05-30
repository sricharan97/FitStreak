package com.apptimistiq.android.fitstreak.main.dashboard.di

import com.apptimistiq.android.fitstreak.authentication.di.AuthenticationModule
import com.apptimistiq.android.fitstreak.main.dashboard.DashboardFragment
import dagger.Subcomponent

/**
 * Dagger Subcomponent that provides dependencies for the Dashboard feature.
 *
 * This component is responsible for injecting dependencies into the Dashboard-related
 * classes, including the DashboardFragment. It uses DashboardModule for providing
 * Dashboard-specific dependencies.
 */
@Subcomponent(modules = [DashboardModule::class, AuthenticationModule::class])
interface DashboardComponent {

    /**
     * Factory interface for creating instances of DashboardComponent.
     *
     * This follows the Dagger Subcomponent Factory pattern for component instantiation.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates a new instance of the DashboardComponent
         *
         * @return A new DashboardComponent instance
         */
        fun create(): DashboardComponent
    }

    /**
     * Injects dependencies into the given DashboardFragment
     *
     * @param fragment The fragment where dependencies should be injected
     */
    fun inject(fragment: DashboardFragment)
}
