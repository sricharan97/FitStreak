package com.apptimistiq.android.fitstreak

import android.app.Application
import com.apptimistiq.android.fitstreak.di.AppComponent
import com.apptimistiq.android.fitstreak.di.DaggerAppComponent

/**
 * Custom Application class for the FitStreak app.
 * 
 * This class is responsible for initializing app-wide dependencies using Dagger
 * and provides access to the dependency injection graph through [appComponent].
 * It's registered in the AndroidManifest.xml to be used as the application class.
 */
open class FitApp : Application() {

    /**
     * The application's DI component that provides app-level dependencies.
     * Lazily initialized to ensure it's only created when needed.
     */
     val appComponent: AppComponent by lazy {
        initializeComponent()
    }

    /**
     * Initializes and creates the Dagger AppComponent.
     *
     * @return The created AppComponent instance
     */
    open fun initializeComponent(): AppComponent {
        // Create the AppComponent using the Dagger generated factory
        // and provide application context that will be available in the DI graph
        return DaggerAppComponent.factory().create(applicationContext)
    }
}
