package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * AuthDataSource interface defines the contract for authentication-related operations.
 * This interface abstracts the authentication logic, making it easier to test and maintain.
 */
interface AuthDataSource {

    /**
     * Retrieves the currently authenticated user.
     *
     * @return [FirebaseUser] if a user is authenticated, or null otherwise.
     */
    suspend fun getCurrentUser(): FirebaseUser?

    /**
     * Finalizes the authentication process and retrieves the user's state information.
     *
     * @return [UserStateInfo] containing the user's authentication state details.
     */
    suspend fun finalizeAuthentication(): UserStateInfo

    /**
     * Observes changes in the authentication state.
     *
     * @return [Flow] emitting [UserStateInfo] whenever the authentication state changes.
     */
    fun observeAuthState(): Flow<UserStateInfo>

    /**
     * Signs out the currently authenticated user.
     */
    suspend fun signOut()

    /**
     * Signs out the currently authenticated user and resets their onboarding status and goal data.
     * This is useful for testing the login and onboarding flow without uninstalling the app.
     */
    suspend fun signOutAndResetData()
}
