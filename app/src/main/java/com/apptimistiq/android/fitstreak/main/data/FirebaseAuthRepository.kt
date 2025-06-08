package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val userProfileDataSource: UserProfileDataSource
) : AuthDataSource {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun updateLocalUserAfterLogin(): UserStateInfo {
        val firebaseUser = firebaseAuth.currentUser
        val currentUserState = userProfileDataSource.userStateInfo.first()

        // If we have a Firebase user, update local storage with fresh Firebase data,
        // preserving existing onboarding status.
        val updatedState = firebaseUser?.let { user ->
            currentUserState.copy(
                uid = user.uid,
                // Use Firebase display name if available and not blank, otherwise keep existing local name.
                userName = user.displayName?.takeIf { it.isNotBlank() } ?: currentUserState.userName,
                isUserLoggedIn = true
                // isOnboarded status is preserved from currentUserState
            )
        } ?: currentUserState.copy(isUserLoggedIn = false) // Fallback if firebaseUser is null after login (should not happen)

        userProfileDataSource.updateUserStateInfo(updatedState)
        return updatedState // Return the state that was just saved
    }

    override fun observeUserLoginState(): Flow<Boolean> {
        // The user's login status is derived from the UserStateInfo in local DataStore.
        return userProfileDataSource.userStateInfo.map { it.isUserLoggedIn }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        // Update local storage: mark as not logged in and clear UID.
        // Other details like username and onboarding status are preserved.
        val currentUserState = userProfileDataSource.userStateInfo.first()
        userProfileDataSource.updateUserStateInfo(
            currentUserState.copy(isUserLoggedIn = false, uid = "")
        )
    }

    override suspend fun signOutAndResetAllUserData() {
        firebaseAuth.signOut()
        // Reset fitness goals and user-specific profile data (height, weight, diet).
        userProfileDataSource.resetOnboardingAndGoalData()
        // Reset all fields of UserStateInfo to their default, logged-out, non-onboarded values.
        userProfileDataSource.updateUserStateInfo(
            UserStateInfo(
                uid = "",
                userName = "User", // Default username
                isUserLoggedIn = false,
                isOnboarded = false
            )
        )
    }
}