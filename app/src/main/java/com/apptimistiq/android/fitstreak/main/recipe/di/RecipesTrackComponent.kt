package com.apptimistiq.android.fitstreak.main.recipe.di

import com.apptimistiq.android.fitstreak.main.recipe.RecipesFragment
import dagger.Subcomponent

/**
 * Dagger Subcomponent for Recipes functionality.
 *
 * This component provides dependencies required by the recipes feature,
 * extending the object graph with recipe-specific dependencies while inheriting
 * from its parent component.
 */
@Subcomponent(modules = [RecipesTrackModule::class])
interface RecipesTrackComponent {

    /**
     * Factory interface for creating instances of [RecipesTrackComponent].
     *
     * Dagger will generate an implementation of this interface to instantiate
     * the component with all required dependencies.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates a new instance of [RecipesTrackComponent].
         *
         * @return A new instance of RecipesTrackComponent
         */
        fun create(): RecipesTrackComponent
    }

    /**
     * Injects dependencies into the specified [RecipesFragment].
     *
     * @param fragment The fragment where dependencies will be injected
     */
    fun inject(fragment: RecipesFragment)
}
