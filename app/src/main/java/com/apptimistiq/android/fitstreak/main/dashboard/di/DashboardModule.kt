package com.apptimistiq.android.fitstreak.main.dashboard.di

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import com.apptimistiq.android.fitstreak.main.dashboard.DashboardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Dagger Module that provides Dashboard feature dependencies.
 *
 * This module contributes to the DashboardComponent and provides
 * Dashboard-related dependencies like ViewModels. It uses Dagger's multibinding
 * to add the DashboardViewModel to the app's ViewModel factory.
 */
@Module
abstract class DashboardModule {

    /**
     * Binds the DashboardViewModel implementation to the ViewModel abstract class.
     *
     * Uses Dagger multibinding with IntoMap and ViewModelKey to make this ViewModel
     * available through the ViewModelProvider.Factory.
     *
     * @param viewModel The DashboardViewModel instance to be provided
     * @return The bound ViewModel instance
     */
    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    abstract fun bindViewModel(viewModel: DashboardViewModel): ViewModel
}
