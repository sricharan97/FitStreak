package com.apptimistiq.android.fitstreak.main.progressTrack.di

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import com.apptimistiq.android.fitstreak.main.progressTrack.ProgressViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Dagger Module for providing Daily Progress feature dependencies.
 * 
 * This module contains bindings for the ViewModels and other dependencies
 * required by the Daily Progress feature. It uses abstract methods with
 * [Binds] annotation for efficient dependency provision.
 */
@Module
abstract class DailyProgressModule {

    /**
     * Binds the concrete implementation of [ProgressViewModel] to the generic [ViewModel].
     *
     * This method enables Dagger to include this ViewModel in the multi-binding map
     * for the ViewModelFactory, allowing automatic ViewModel instantiation.
     *
     * @param viewModel The concrete implementation of ProgressViewModel to bind
     * @return The bound ViewModel instance
     */
    @Binds
    @IntoMap
    @ViewModelKey(ProgressViewModel::class)
    abstract fun bindViewModel(viewModel: ProgressViewModel): ViewModel
}
