package com.apptimistiq.android.fitstreak.main.recipe.di

import com.apptimistiq.android.fitstreak.main.recipe.RecipesFragment
import dagger.Subcomponent


@Subcomponent(modules = [RecipesTrackModule::class])
interface RecipesTrackComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): RecipesTrackComponent
    }


    fun inject(fragment: RecipesFragment)
}