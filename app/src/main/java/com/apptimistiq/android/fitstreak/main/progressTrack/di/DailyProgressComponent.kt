package com.apptimistiq.android.fitstreak.main.progressTrack.di

import com.apptimistiq.android.fitstreak.main.progressTrack.DailyProgressFragment
import dagger.Subcomponent

/**
 * Dagger Subcomponent for Daily Progress feature.
 * 
 * This component provides dependencies for the Daily Progress feature,
 * encapsulating all required dependencies from parent component
 * and adding feature-specific dependencies.
 */
@Subcomponent(modules = [DailyProgressModule::class])
interface DailyProgressComponent {

    /**
     * Factory interface for creating instances of [DailyProgressComponent].
     * 
     * Implementing this interface allows Dagger to create the component
     * with proper scope and dependencies from the parent component.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates a new instance of [DailyProgressComponent].
         *
         * @return A configured [DailyProgressComponent] instance
         */
        fun create(): DailyProgressComponent
    }

    /**
     * Injects dependencies into the specified fragment.
     *
     * @param fragment The fragment to inject dependencies into
     */
    fun inject(fragment: DailyProgressFragment)
}
