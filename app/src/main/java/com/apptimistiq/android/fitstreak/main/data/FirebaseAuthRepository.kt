package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val userProfileDataSource: UserProfileDataSource
) : AuthDataSource {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override suspend fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun finalizeAuthentication(): UserStateInfo {
        val firebaseUser = getCurrentUser()
        val currentUserState = userProfileDataSource.userStateInfo.first()
        
        // If we have a Firebase user, update local storage with fresh Firebase data
        val updatedState = firebaseUser?.let { user ->
            UserStateInfo(
                uid = user.uid,
                // Use Firebase display name if available, otherwise keep existing name
                userName = user.displayName?.takeIf { it.isNotBlank() } ?: currentUserState.userName,
                isUserLoggedIn = true,
                isOnboarded = currentUserState.isOnboarded
            )
        } ?: UserStateInfo() // Reset to default if no user
        
        // Update local storage with new state
        userProfileDataSource.updateUserStateInfo(updatedState)
        return updatedState
    }

    override fun observeAuthState(): Flow<UserStateInfo> {
        // We're using the local DataStore as source of truth for auth state
        return userProfileDataSource.userStateInfo
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        // Update local storage to reflect signed out state
        userProfileDataSource.updateUserStateInfo(
            UserStateInfo(isUserLoggedIn = false)
        )
    }

    /**
     * Signs out the currently authenticated user and resets all user data.
     * This is useful for testing the onboarding and login flow without
     * having to uninstall the app.
     */
    override suspend fun signOutAndResetData() {
        firebaseAuth.signOut()
        userProfileDataSource.resetOnboardingAndGoalData() // Reset goals and user info (height, weight, diet)
        userProfileDataSource.updateUserStateInfo(
            UserStateInfo(isUserLoggedIn = false, isOnboarded = false, userName = "User", uid = "") // Reset all user state
        )
    }
}
