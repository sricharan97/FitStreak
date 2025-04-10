package com.apptimistiq.android.fitstreak.main.home.di

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import com.apptimistiq.android.fitstreak.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Dagger module that provides ViewModel bindings for the Home Transition feature.
 * 
 * This module contributes to the dependency graph by binding the MainViewModel
 * to the ViewModel map using the ViewModelKey annotation.
 */
@Module
abstract class HomeTransitionModule {
    
    /**
     * Binds MainViewModel instance to the ViewModels map
     * to be provided by the ViewModelFactory.
     * 
     * @param viewModel The MainViewModel instance to be provided
     * @return ViewModel The bound ViewModel
     */
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindViewModel(viewModel: MainViewModel): ViewModel
}
