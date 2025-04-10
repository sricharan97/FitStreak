package com.apptimistiq.android.fitstreak.main.progressTrack.di

import com.apptimistiq.android.fitstreak.main.progressTrack.EditActivityFragment
import dagger.Subcomponent

/**
 * Dagger Subcomponent for Edit Activity feature.
 * 
 * This component provides dependencies for the Edit Activity feature,
 * reusing the same module as DailyProgressComponent since they share
 * common dependencies.
 */
@Subcomponent(modules = [DailyProgressModule::class])
interface EditActivityComponent {

    /**
     * Factory interface for creating instances of [EditActivityComponent].
     * 
     * Implementing this interface allows Dagger to create the component
     * with proper scope and dependencies from the parent component.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates a new instance of [EditActivityComponent].
         *
         * @return A configured [EditActivityComponent] instance
         */
        fun create(): EditActivityComponent
    }

    /**
     * Injects dependencies into the specified fragment.
     *
     * @param fragment The fragment to inject dependencies into
     */
    fun inject(fragment: EditActivityFragment)
}
