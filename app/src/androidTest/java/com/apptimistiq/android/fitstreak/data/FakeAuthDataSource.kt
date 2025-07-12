// app/src/androidTest/java/com/apptimistiq/android/fitstreak/data/FakeAuthDataSource.kt

package com.apptimistiq.android.fitstreak.data

import com.apptimistiq.android.fitstreak.main.data.AuthDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAuthDataSource : AuthDataSource {

    private var firebaseUser: FirebaseUser? = null
    private val userState = MutableStateFlow(UserStateInfo())
    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    fun setFirebaseUser(user: FirebaseUser?) {
        this.firebaseUser = user
    }

    fun setUserState(state: UserStateInfo) {
        userState.value = state
    }


    override fun getCurrentFirebaseUser(): FirebaseUser? {
        if (shouldReturnError) throw Exception("Test exception")
        return firebaseUser
    }

    override suspend fun updateLocalUserAfterLogin(): UserStateInfo {
        if (shouldReturnError) throw Exception("Test exception")
        val currentState = userState.value
        val updatedState = firebaseUser?.let { user ->
            // Correctly merge the new login info with the existing isOnboarded state.
            currentState.copy(
                uid = user.uid,
                userName = user.displayName?.takeIf { it.isNotBlank() } ?: currentState.userName,
                isUserLoggedIn = true,
                isOnboarded = currentState.isOnboarded // Explicitly preserve the isOnboarded flag
            )
        } ?: currentState.copy(isUserLoggedIn = false)

        userState.value = updatedState
        return updatedState
    }

    override fun observeUserLoginState(): Flow<Boolean> {
        return userState.map {
            if (shouldReturnError) throw Exception("Test exception")
            it.isUserLoggedIn
        }
    }

    override suspend fun signOut() {
        if (shouldReturnError) throw Exception("Test exception")
        firebaseUser = null
        val currentState = userState.value
        userState.value = currentState.copy(isUserLoggedIn = false, uid = "")
    }

    override suspend fun signOutAndResetAllUserData() {
        if (shouldReturnError) throw Exception("Test exception")
        firebaseUser = null
        userState.value = UserStateInfo(
            uid = "",
            userName = "User",
            isUserLoggedIn = false,
            isOnboarded = false
        )
    }

    fun clear() {
        firebaseUser = null
        userState.value = UserStateInfo()
        shouldReturnError = false
    }
}