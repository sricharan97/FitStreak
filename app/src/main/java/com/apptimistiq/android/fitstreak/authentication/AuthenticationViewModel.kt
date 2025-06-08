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
import com.apptimistiq.android.fitstreak.main.data.AuthDataSource
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
 * @property authDataSource The data source for authentication operations
 */
class AuthenticationViewModel @Inject constructor(
    private val dataSource: ActivityDataSource,
    private val authDataSource: AuthDataSource
) : ViewModel() {

    /**
     * StateFlow representing the current user state information.
     */
    val userState: StateFlow<AuthDataResult<UserStateInfo>> = dataSource.getCurrentUserState()
        .map{ AuthDataResult.Success(it) as AuthDataResult<UserStateInfo> }
        .catch { emit(AuthDataResult.Error(it)) } // Handle potential errors from the flow
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthDataResult.Loading
    )

    /**
     * Check if the user is currently authenticated
     */
    val isAuthenticated: StateFlow<AuthDataResult<Boolean>> = authDataSource.observeUserLoginState()
        .map { AuthDataResult.Success(it) as AuthDataResult<Boolean> }
        .catch { emit(AuthDataResult.Error(it)) } // Handle potential errors from the flow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthDataResult.Loading
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

    /**
     * Finalizes the authentication process by:
     * 1. Getting the current Firebase user
     * 2. Updating the UserStateInfo with authentication data
     * 3. Returning the updated user state
     * 
     * This consolidates Firebase Auth data with local preferences.
     */
    suspend fun finalizeAuthentication(): UserStateInfo {
        return authDataSource.updateLocalUserAfterLogin()
    }

    /**
     * Signs the current user out from Firebase and updates local state
     */
    fun signOut() {
        viewModelScope.launch {
            authDataSource.signOut()
        }
    }

    /**
     * Signs the current user out, resets their data and updates local state.
     * This is useful for testing the login and onboarding flows without
     * having to uninstall the app.
     */
    fun signOutAndResetData() {
        viewModelScope.launch {
            authDataSource.signOutAndResetAllUserData()
        }
    }
}

sealed interface AuthDataResult<out T>{
    data class Success<out T>(val data: T) : AuthDataResult<T>
    data class Error(val exception: Throwable) : AuthDataResult<Nothing>
    data object Loading : AuthDataResult<Nothing>
}
