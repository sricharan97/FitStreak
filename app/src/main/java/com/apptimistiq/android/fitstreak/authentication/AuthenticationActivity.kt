package com.apptimistiq.android.fitstreak.authentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apptimistiq.android.fitstreak.R

/**
 * Authentication activity that handles user login and registration.
 *
 * This activity serves as the entry point for authentication flows in the FitStreak application.
 * It displays the authentication UI and manages the authentication process.
 */
class AuthenticationActivity : AppCompatActivity() {

    /**
     * Initializes the activity, sets the content view and configures the authentication UI.
     *
     * @param savedInstanceState Contains data supplied if the activity is being re-initialized
     *                          after previously being shut down.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
    }
}
