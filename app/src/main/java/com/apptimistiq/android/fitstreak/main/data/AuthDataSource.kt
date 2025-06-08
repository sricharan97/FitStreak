package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {

    /**
     * Retrieves the current FirebaseUser if one is authenticated.
     * @return The current FirebaseUser, or null if no user is signed in.
     */
    fun getCurrentFirebaseUser(): FirebaseUser?

    /**
     * Call this after a successful Firebase sign-in.
     * It fetches the current Firebase user, merges its details (UID, display name)
     * with the existing local UserStateInfo (preserving onboarding status),
     * marks the user as logged in, and saves this updated state to UserProfileDataSource.
     * @return The updated UserStateInfo after synchronization.
     */
    suspend fun updateLocalUserAfterLogin(): UserStateInfo

    /**
     * Observes the user's login state (isUserLoggedIn).
     * This flow emits true if the user is considered logged in based on local data, false otherwise.
     * @return A Flow emitting the boolean login status.
     */
    fun observeUserLoginState(): Flow<Boolean>

    /**
     * Signs the current user out from Firebase and updates local state
     * to reflect that the user is no longer logged in (isUserLoggedIn = false, uid is cleared).
     * Other user data like onboarding status and profile info might be preserved.
     */
    suspend fun signOut()

    /**
     * Signs the current user out from Firebase, resets all onboarding progress,
     * fitness goals, and user profile information (height, weight, diet) to their defaults.
     * The user state is fully reset to a logged-out, non-onboarded state.
     */
    suspend fun signOutAndResetAllUserData()
}