package com.apptimistiq.android.fitstreak.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.domain.MainUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the main application flow.
 * Uses MainUIState as the single source of truth for all UI-related state.
 */
class MainViewModel @Inject constructor() : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()


    /**
     * Handles the result of the permission request and updates relevant states.
     */
    fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            _uiState.update { currentState ->
                currentState.copy(
                    navigateToDailyProgress = true,
                    upgradeHomeFunctionality = true,
                    degradeHomeFunctionality = false,
                    isActivityPermissionDenied = false,
                    isHomeScreenDegraded = false,
                    showPermissionDeniedMessage = false
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    navigateToDailyProgress = false,
                    degradeHomeFunctionality = true,
                    upgradeHomeFunctionality = false,
                    isActivityPermissionDenied = true,
                    isHomeScreenDegraded = true,
                    showPermissionDeniedMessage = true
                )
            }
        }
    }

    /**
     * Activates permission status check
     */
    fun activatePermissionStatusCheck() {
        _uiState.update { it.copy(isPermissionCheckInProgress = true) }
    }


    /**
     * Resets the activity permission denied flag
     */
    fun resetActivityPermissionDenied() {
        _uiState.update {
            it.copy(
                isActivityPermissionDenied = false,
                showPermissionDeniedMessage = false
            )
        }
    }

    /**
     * Called when activity permission check has completed
     */
    fun activityPermissionCheckComplete() {
        _uiState.update { it.copy(isPermissionCheckInProgress = false) }
    }

    /**
     * Signals readiness to navigate to DailyProgress screen
     * Note: Not used directly but kept for API compatibility
     */
    fun readyToNavigateToDailyProgress() {
        _uiState.update { it.copy(navigateToDailyProgress = true) }
    }

    /**
     * Resets navigation flag after navigation to DailyProgress is complete
     */
    fun navigationToDailyProgressComplete() {
        _uiState.update { it.copy(navigateToDailyProgress = false) }
    }

    /**
     * Sets flag to degrade home destination menu functionality
     */
    fun degradeHomeDestinationMenu() {
        _uiState.update {
            it.copy(
                isHomeScreenDegraded = true,
                degradeHomeFunctionality = true
            )
        }
    }

    /**
     * Sets flag to upgrade home destination menu functionality
     */
    fun upgradeHomeDestinationMenu() {
        _uiState.update {
            it.copy(
                isHomeScreenDegraded = false,
                upgradeHomeFunctionality = true
            )
        }
    }

    /**
     * Resets the home functionality upgrade flag
     * Note: Not used directly but kept for API compatibility
     */
    fun resetUpgradedHomeDestinationMap() {
        _uiState.update { it.copy(upgradeHomeFunctionality = false) }
    }

    /**
     * Resets the home functionality degradation flag
     * Note: Not used directly but kept for API compatibility
     */
    fun resetHomeDestinationMap() {
        _uiState.update { it.copy(degradeHomeFunctionality = false) }
    }

    /**
     * Sets bottom navigation visibility
     */
    fun setBottomNavVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(bottomNavVisible = isVisible) }
    }
}