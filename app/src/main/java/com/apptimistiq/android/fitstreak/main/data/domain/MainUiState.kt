package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * UI State that represents all the UI-related states
 * This will survive configuration changes
 */
data class MainUIState(
    val bottomNavVisible: Boolean = true,
    val isHomeScreenDegraded: Boolean = false,
    val showPermissionDeniedMessage: Boolean = false,
    val isPermissionCheckInProgress: Boolean = false,
    val navigateToDailyProgress: Boolean = false,
    val isActivityPermissionDenied: Boolean = false,
    val upgradeHomeFunctionality: Boolean = false,
    val degradeHomeFunctionality: Boolean = false
)
