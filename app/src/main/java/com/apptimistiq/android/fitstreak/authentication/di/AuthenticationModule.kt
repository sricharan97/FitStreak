package com.apptimistiq.android.fitstreak.authentication.di

import androidx.lifecycle.ViewModel
import com.apptimistiq.android.fitstreak.authentication.AuthenticationViewModel
import com.apptimistiq.android.fitstreak.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthenticationModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthenticationViewModel::class)
    abstract fun bindViewModel(viewModel: AuthenticationViewModel): ViewModel

}