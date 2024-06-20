package com.apptimistiq.android.fitstreak.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {


    //Keep track of the activity recognition permission status
    private val _activityPermissionStatusCheck = MutableStateFlow(false)
    val activityPermissionStatusCheck = _activityPermissionStatusCheck

    //Map the bottom nav home destination to daily progress fragment once the permission check is complete
    private val _degradeHomeFunctionality = MutableStateFlow(false)
    val degradeHomeFunctionality = _degradeHomeFunctionality

    private val _upgradeHomeFunctionality = MutableStateFlow(false)
    val upgradeHomeFunctionality = _upgradeHomeFunctionality


    //Keep track of the navigation state to DailyProgress screen
    private val _navigateToDailyProgress = MutableStateFlow(false)
    val navigateToDailyProgress = _navigateToDailyProgress

    //Keep track of the permission status for activity recognition
    private val _activityPermissionDenied = MutableStateFlow(false)
    val activityPermissionDenied = _activityPermissionDenied

    init {
        _activityPermissionStatusCheck.value = true
    }

    //ready to navigate to DailyProgress screen from the homeTransition Fragment
    fun readyToNavigateToDailyProgress() {
        _navigateToDailyProgress.value = true
    }

    //Completed the navigation to DailyProgress screen
    fun navigationToDailyProgressComplete() {
        _navigateToDailyProgress.value = false
    }

    //Permission denied for activity recognition
    fun activityPermissionDenied() {
        _activityPermissionDenied.value = true
    }

    //reset the permission denied status
    fun resetActivityPermissionDenied() {
        _activityPermissionDenied.value = false
    }

    //activity permission check has completed
    fun activityPermissionCheckComplete() {
        _activityPermissionStatusCheck.value = false
        _degradeHomeFunctionality.value = true
    }

    fun degradeHomeDestinationMenu() {
        _degradeHomeFunctionality.value = true
    }

    fun upgradeHomeDestinationMenu() {
        _upgradeHomeFunctionality.value = true
    }

    fun resetUpgradedHomeDestinationMap() {
        _upgradeHomeFunctionality.value = false
    }

    fun resetHomeDestinationMap() {
        _degradeHomeFunctionality.value = false
    }

}