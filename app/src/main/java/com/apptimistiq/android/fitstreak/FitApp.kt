package com.apptimistiq.android.fitstreak

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val GOAL_PREFERENCES_NAME = "goal_preferences"

class FitApp : Application() {

    private val Context.dataStore by preferencesDataStore(name = GOAL_PREFERENCES_NAME)


}