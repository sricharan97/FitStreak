package com.apptimistiq.android.fitstreak.main.diMain

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import com.apptimistiq.android.fitstreak.main.MainViewModel
import com.apptimistiq.android.fitstreak.main.home.SplashViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap


/**
 * Dagger module that provides ViewModel bindings for the Home Transition feature.
 *
 * This module contributes to the dependency graph by binding the MainViewModel
 * to the ViewModel map using the ViewModelKey annotation.
 */
@Module
object SplashViewModelModule {

    @Provides
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    fun provideSplashViewModel(): ViewModel {
        return SplashViewModel()
    }
}