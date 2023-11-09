package com.apptimistiq.android.fitstreak.main.dashboard.di

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import com.apptimistiq.android.fitstreak.main.dashboard.DashboardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
abstract class DashboardModule {

    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    abstract fun bindViewModel(viewModel: DashboardViewModel): ViewModel
}