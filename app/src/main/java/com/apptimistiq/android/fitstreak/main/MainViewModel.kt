package com.apptimistiq.android.fitstreak.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * ViewModel for the main application flow.
 * 
 * Manages states related to:
 * - Activity recognition permissions
 * - Navigation between main app destinations
 * - Home screen functionality states
 */
class MainViewModel @Inject constructor() : ViewModel() {

    //region Permission States
    /**
     * Tracks if activity recognition permission check is in progress
     * True when checking, false when check is complete
     */
    private val _activityPermissionStatusCheck = MutableStateFlow(false)
    val activityPermissionStatusCheck = _activityPermissionStatusCheck

    /**
     * Indicates if activity recognition permission was denied by the user
     */
    private val _activityPermissionDenied = MutableStateFlow(false)
    val activityPermissionDenied = _activityPermissionDenied
    //endregion

    //region Navigation States
    /**
     * Tracks when navigation to DailyProgress screen should occur
     */
    private val _navigateToDailyProgress = MutableStateFlow(false)
    val navigateToDailyProgress = _navigateToDailyProgress
    //endregion

    //region Home Functionality States
    /**
     * Controls whether home functionality should be reduced due to permission issues
     */
    private val _degradeHomeFunctionality = MutableStateFlow(false)
    val degradeHomeFunctionality = _degradeHomeFunctionality

    /**
     * Controls whether home functionality should be enhanced after permissions granted
     */
    private val _upgradeHomeFunctionality = MutableStateFlow(false)
    val upgradeHomeFunctionality = _upgradeHomeFunctionality
    //endregion

    init {
        _activityPermissionStatusCheck.value = true
    }

    //region Permission Management Methods
    /**
     * Marks that activity recognition permission is denied by the user
     */
    fun activityPermissionDenied() {
        _activityPermissionDenied.value = true
    }

    /**
     * Resets the activity permission denied flag
     */
    fun resetActivityPermissionDenied() {
        _activityPermissionDenied.value = false
    }

    /**
     * Called when activity permission check has completed
     * Updates status check flag and degrades home functionality if needed
     */
    fun activityPermissionCheckComplete() {
        _activityPermissionStatusCheck.value = false
        _degradeHomeFunctionality.value = true
    }
    //endregion

    //region Navigation Methods
    /**
     * Signals readiness to navigate to DailyProgress screen from the homeTransition Fragment
     */
    fun readyToNavigateToDailyProgress() {
        _navigateToDailyProgress.value = true
    }

    /**
     * Resets navigation flag after navigation to DailyProgress is complete
     */
    fun navigationToDailyProgressComplete() {
        _navigateToDailyProgress.value = false
    }
    //endregion

    //region Home Functionality Methods
    /**
     * Sets flag to degrade home destination menu functionality
     */
    fun degradeHomeDestinationMenu() {
        _degradeHomeFunctionality.value = true
    }

    /**
     * Sets flag to upgrade home destination menu functionality
     */
    fun upgradeHomeDestinationMenu() {
        _upgradeHomeFunctionality.value = true
    }

    /**
     * Resets the home functionality upgrade flag
     */
    fun resetUpgradedHomeDestinationMap() {
        _upgradeHomeFunctionality.value = false
    }

    /**
     * Resets the home functionality degradation flag
     */
    fun resetHomeDestinationMap() {
        _degradeHomeFunctionality.value = false
    }
    //endregion
}
