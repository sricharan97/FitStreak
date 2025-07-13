/*
 * Copyright (c) 2023 Apptimistiq.
 * All rights reserved.
 *
 * MainActivityComponent.kt
 * Dagger component for MainActivity dependency injection
 */

package com.apptimistiq.android.fitstreak.main.diMain

import com.apptimistiq.android.fitstreak.authentication.di.AuthenticationModule
import com.apptimistiq.android.fitstreak.main.MainActivity
import com.apptimistiq.android.fitstreak.main.home.di.HomeTransitionModule
import dagger.Subcomponent

/**
 * Dagger Subcomponent that provides dependencies for the MainActivity.
 * Extends functionality by including modules required for transitions 
 * and other main activity related dependencies.
 */
@Subcomponent(modules = [HomeTransitionModule::class, AuthenticationModule::class])
interface MainActivityComponent {

    /**
     * Factory interface used to create instances of MainActivityComponent.
     * This follows the Dagger factory pattern for subcomponents.
     */
    @Subcomponent.Factory
    interface Factory {
        /**
         * Creates an instance of the MainActivityComponent.
         *
         * @return A new instance of MainActivityComponent
         */
        fun create(): MainActivityComponent
    }

    /**
     * Injects dependencies into the specified MainActivity instance.
     *
     * @param mainActivity The MainActivity instance that needs dependency injection
     */
    fun inject(mainActivity: MainActivity)
}
