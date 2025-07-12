package com.apptimistiq.android.fitstreak

import com.apptimistiq.android.fitstreak.di.AppComponent
import com.apptimistiq.android.fitstreak.di.DaggerTestApplicationComponent

class TestApplication : FitApp() {

    override fun initializeComponent() : AppComponent {
        return DaggerTestApplicationComponent.create() as AppComponent

    }

}