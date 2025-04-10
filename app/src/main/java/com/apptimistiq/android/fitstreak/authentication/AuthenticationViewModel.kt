/*
 * AuthenticationViewModel.kt
 * FitStreak Project
 *
 * This file contains the ViewModel for handling authentication and user state management.
 * It interfaces with the ActivityDataSource to store and retrieve user-related data.
 */
package com.apptimistiq.android.fitstreak.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.GoalType
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that manages authentication state and user data for the authentication screens.
 * 
 * Responsible for:
 * - Exposing the current user state
 * - Persisting user fitness goals
 * - Saving user profile information
 *
 * @property dataSource The data source for accessing and storing activity and user data
 */
class AuthenticationViewModel @Inject constructor(
    private val dataSource: ActivityDataSource
) : ViewModel() {

    /**
     * StateFlow representing the current user state information.
     * Initialized eagerly to make it immediately available to the UI.
     */
    val userState: StateFlow<UserStateInfo> = dataSource.getCurrentUserState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserStateInfo()
    )

    /**
     * Persists a user's fitness goal to the data source.
     * 
     * @param goalType The type of goal being set (e.g., STEPS, CALORIES)
     * @param value The numerical target value for the goal
     */
    fun saveGoal(goalType: GoalType, value: Int) {
        viewModelScope.launch {
            dataSource.saveGoal(goalType, value)
        }
    }

    /**
     * Saves the complete user state information to the data source.
     * This includes profile details and preferences.
     *
     * @param userStateInfo The complete user state information to be saved
     */
    fun saveUserStateInfo(userStateInfo: UserStateInfo) {
        viewModelScope.launch {
            dataSource.saveUserState(userStateInfo)
        }
    }
}
