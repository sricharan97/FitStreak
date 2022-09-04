package com.apptimistiq.android.fitstreak.main.progressTrack.di

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import com.apptimistiq.android.fitstreak.main.progressTrack.ProgressViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DailyProgressModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProgressViewModel::class)
    abstract fun bindViewModel(viewModel: ProgressViewModel): ViewModel
}