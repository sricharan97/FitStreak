package com.apptimistiq.android.fitstreak.main.recipe.di

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import com.apptimistiq.android.fitstreak.main.recipe.RecipeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class RecipesTrackModule {


    @Binds
    @IntoMap
    @ViewModelKey(RecipeViewModel::class)
    abstract fun bindViewModel(viewModel: RecipeViewModel): ViewModel
}