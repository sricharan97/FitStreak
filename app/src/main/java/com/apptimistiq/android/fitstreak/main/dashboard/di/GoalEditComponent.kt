package com.apptimistiq.android.fitstreak.main.dashboard.di

import com.apptimistiq.android.fitstreak.main.dashboard.GoalEditFragment
import dagger.Subcomponent

/**
 * Dagger Subcomponent that provides dependencies for the Goal Edit feature.
 *
 * This component is responsible for injecting dependencies into the Goal Edit-related
 * classes, specifically the GoalEditFragment. It uses the DashboardModule for providing
 * required dependencies since goal editing is part of the dashboard feature.
 */
@Subcomponent(modules = [DashboardModule::class])
interface GoalEditComponent {

    /**
     * Factory interface for creating instances of GoalEditComponent.
     *
     * This follows the Dagger Subcomponent Factory pattern for component instantiation.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates a new instance of the GoalEditComponent
         *
         * @return A new GoalEditComponent instance
         */
        fun create(): GoalEditComponent
    }

    /**
     * Injects dependencies into the given GoalEditFragment
     *
     * @param fragment The fragment where dependencies should be injected
     */
    fun inject(fragment: GoalEditFragment)
}
