package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * UI State that represents all the UI-related states
 * This will survive configuration changes
 */
data class MainUIState(
    val isActivityPermissionGranted: Boolean = false,
    val bottomNavVisible: Boolean = false
)
