package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import com.apptimistiq.android.fitstreak.main.data.test.FakeUserProfileDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@ExperimentalCoroutinesApi
class FirebaseAuthRepositoryTest {
    // Subject under test
    private lateinit var repository: FirebaseAuthRepository

    // Fake dependencies
    private lateinit var fakeUserProfileDataSource: FakeUserProfileDataSource
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser

    // Test dispatcher
    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val defaultUserState = UserStateInfo(
        uid = "existing-uid",
        userName = "Existing User",
        isUserLoggedIn = false,
        isOnboarded = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks and fakes
        fakeUserProfileDataSource = FakeUserProfileDataSource()
        mockFirebaseAuth = mock(FirebaseAuth::class.java)
        mockFirebaseUser = mock(FirebaseUser::class.java)

        // Create repository with mock dependencies - no reflection needed
        repository = FirebaseAuthRepository(
            fakeUserProfileDataSource,
            mockFirebaseAuth
        )

        setupDefaultTestData()
    }

    private fun setupDefaultTestData() {
        // Set up initial user state
        fakeUserProfileDataSource.setUserStateInfo(defaultUserState)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getCurrentFirebaseUser_returnsCurrentUserFromFirebaseAuth() = runTest {
        // Given: Firebase Auth has a current user
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        // When: Getting current Firebase user
        val result = repository.getCurrentFirebaseUser()

        // Then: Should return the Firebase user
        assertEquals(mockFirebaseUser, result)
    }

    @Test
    fun getCurrentFirebaseUser_whenNoUserLoggedIn_returnsNull() = runTest {
        // Given: Firebase Auth has no current user
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // When: Getting current Firebase user
        val result = repository.getCurrentFirebaseUser()

        // Then: Should return null
        assertNull(result)
    }

    @Test
    fun updateLocalUserAfterLogin_whenFirebaseUserExists_updatesLocalUser() = runTest {
        // Given: Firebase Auth has a current user with valid info
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn("firebase-uid")
        `when`(mockFirebaseUser.displayName).thenReturn("Firebase User")

        // When: Updating local user after login
        val result = repository.updateLocalUserAfterLogin()

        // Then: Should update local user with Firebase data
        assertEquals("firebase-uid", result.uid)
        assertEquals("Firebase User", result.userName)
        assertTrue(result.isUserLoggedIn)
        // Onboarding status should be preserved from default state
        assertEquals(defaultUserState.isOnboarded, result.isOnboarded)

        // Verify data was saved to data source
        val savedState = fakeUserProfileDataSource.userStateInfo.first()
        assertEquals(result, savedState)
    }

    @Test
    fun updateLocalUserAfterLogin_whenNoFirebaseUser_returnsLocalUserWithLoginFalse() = runTest {
        // Given: Firebase Auth has no current user
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // When: Updating local user after login
        val result = repository.updateLocalUserAfterLogin()

        // Then: Should return local user with isUserLoggedIn = false
        assertFalse(result.isUserLoggedIn)
        // Other values should remain from default state
        assertEquals(defaultUserState.uid, result.uid)
        assertEquals(defaultUserState.userName, result.userName)
        assertEquals(defaultUserState.isOnboarded, result.isOnboarded)
    }

    @Test
    fun updateLocalUserAfterLogin_whenFirebaseUserHasBlankName_preservesExistingName() = runTest {
        // Given: Firebase Auth has a current user with blank display name
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn("firebase-uid")
        `when`(mockFirebaseUser.displayName).thenReturn("")

        // When: Updating local user after login
        val result = repository.updateLocalUserAfterLogin()

        // Then: Should keep existing name from local state
        assertEquals("firebase-uid", result.uid)
        assertEquals(defaultUserState.userName, result.userName) // Keep existing name
        assertTrue(result.isUserLoggedIn)
    }

    @Test
    fun observeUserLoginState_returnsStateFromDataSource() = runTest {
        // Given: Set specific login state in data source
        fakeUserProfileDataSource.setUserStateInfo(defaultUserState.copy(isUserLoggedIn = true))

        // When: Observing login state
        val result = repository.observeUserLoginState().first()

        // Then: Should reflect data source state
        assertTrue(result)
    }

    @Test
    fun signOut_signsOutFirebaseAndUpdatesLocalState() = runTest {
        // Given: Set user as initially logged in
        fakeUserProfileDataSource.setUserStateInfo(defaultUserState.copy(
            uid = "user-123",
            isUserLoggedIn = true
        ))

        // When: Signing out
        repository.signOut()

        // Then: Should call Firebase signOut and update local state
        verify(mockFirebaseAuth).signOut()

        // Check local state is updated correctly
        val updatedState = fakeUserProfileDataSource.userStateInfo.first()
        assertFalse(updatedState.isUserLoggedIn)
        assertEquals("", updatedState.uid)
        // Name and onboarding status should be preserved
        assertEquals(defaultUserState.userName, updatedState.userName)
        assertEquals(defaultUserState.isOnboarded, updatedState.isOnboarded)
    }

    @Test
    fun signOutAndResetAllUserData_resetsAllUserDataToDefaults() = runTest {
        // Given: Set user as initially logged in with specific data
        fakeUserProfileDataSource.setUserStateInfo(defaultUserState.copy(
            isUserLoggedIn = true,
            isOnboarded = true
        ))

        // When: Signing out and resetting data
        repository.signOutAndResetAllUserData()

        // Then: Should call Firebase signOut
        verify(mockFirebaseAuth).signOut()

        // Check local state is fully reset by examining the state directly
        val updatedState = fakeUserProfileDataSource.userStateInfo.first()
        assertFalse(updatedState.isUserLoggedIn)
        assertEquals("", updatedState.uid)
        assertEquals("User", updatedState.userName) // Reset to default name
        assertFalse(updatedState.isOnboarded) // Reset onboarding status

        // Optional: Verify goal preferences were reset by checking values
        val goalPrefs = fakeUserProfileDataSource.goalPreferences.first()
        assertEquals(0, goalPrefs.stepGoal)
        assertEquals(0, goalPrefs.waterGlassGoal)
        assertEquals(0, goalPrefs.sleepGoal)
        assertEquals(0, goalPrefs.exerciseGoal)
    }
}