package com.apptimistiq.android.fitstreak.authentication.di

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Dagger module that provides authentication-related dependencies.
 * This module is used by [LoginComponent] and [AuthenticationComponent] to inject
 * view models and other authentication-related services.
 */
@Module
abstract class AuthenticationModule {

    /**
     * Binds the [AuthenticationViewModel] implementation to the [ViewModel] interface
     * and associates it with its respective key for the ViewModelFactory.
     *
     * @param viewModel The implementation of AuthenticationViewModel to bind
     * @return The bound ViewModel
     */
    @Binds
    @IntoMap
    @ViewModelKey(AuthenticationViewModel::class)
    abstract fun bindViewModel(viewModel: AuthenticationViewModel): ViewModel
}
