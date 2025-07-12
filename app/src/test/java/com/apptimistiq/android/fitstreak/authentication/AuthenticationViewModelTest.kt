package com.apptimistiq.android.fitstreak.authentication

import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.data.FakeAuthDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.GoalType
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

@ExperimentalCoroutinesApi
class AuthenticationViewModelTest {


    //Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    //Subject under test
    private lateinit var viewModel: AuthenticationViewModel

    // Fake dependencies
    private lateinit var fakeActivityDataSource: FakeActivityDataSource
    private lateinit var fakeAuthDataSource: FakeAuthDataSource

    @Before
    fun setup() {
        //Set the main dispatcher to the test dispatcher
        Dispatchers.setMain(testDispatcher)

        //Initialize fakes and the ViewModel
        fakeActivityDataSource = FakeActivityDataSource()
        fakeAuthDataSource = FakeAuthDataSource()
        viewModel = AuthenticationViewModel(fakeActivityDataSource, fakeAuthDataSource)
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher after tests
        Dispatchers.resetMain()
    }


    @Test
    fun `userState - when initialized - emits Loading`() = runTest {
        // Given: The ViewModel is initialized in setup()

        // When: We observe the initial value
        val initialState = viewModel.userState.value

        // Then: The initial state should be Loading
        assertThat(initialState, `is`(instanceOf(AuthDataResult.Loading::class.java)))
    }


    @Test
    fun `userState - when source emits data - emits Success`() = runTest {
        // Given
        val testUserState = UserStateInfo(uid = "test-uid", userName = "Test User", isUserLoggedIn = false, isOnboarded = false)
        val results = mutableListOf<AuthDataResult<UserStateInfo>>()
        val job = launch(testDispatcher) {
            viewModel.userState.collect { results.add(it) }
        }

        // When
        fakeActivityDataSource.setCurrentUserState(testUserState)
        advanceUntilIdle() // Allow the collector to receive the new state

        // Then
        assertThat(results.size, `is`(2))
        assertThat(results[0], `is`(instanceOf(AuthDataResult.Loading::class.java)))
        val successResult = results[1] as AuthDataResult.Success
        assertThat(successResult.data, `is`(testUserState))

        job.cancel()
    }

    @Test
    fun `userState - when source throws error - emits Error`() = runTest {
        // Given
        val results = mutableListOf<AuthDataResult<UserStateInfo>>()
        val job = launch(testDispatcher) {
            viewModel.userState.collect { results.add(it) }
        }

        // When
        fakeActivityDataSource.setShouldReturnError(true)
        // Trigger a new emission to propagate the error
        fakeActivityDataSource.setCurrentUserState(UserStateInfo())
        advanceUntilIdle()

        // Then
        assertThat(results.size, `is`(2))
        assertThat(results[0], `is`(instanceOf(AuthDataResult.Loading::class.java)))
        assertThat(results[1], `is`(instanceOf(AuthDataResult.Error::class.java)))

        job.cancel()
    }

    // region isAuthenticated Tests
    @Test
    fun `isAuthenticated - when initialized - emits Loading`() = runTest {
        // Given: The ViewModel is initialized

        // When: We observe the initial value
        val initialState = viewModel.isAuthenticated.value

        // Then: The initial state should be Loading
        assertThat(initialState, `is`(instanceOf(AuthDataResult.Loading::class.java)))
    }

    @Test
    fun `isAuthenticated - when user logs in and out - emits Success true then false`() = runTest {
        // Given
        val results = mutableListOf<AuthDataResult<Boolean>>()
        val job = launch(testDispatcher) {
            viewModel.isAuthenticated.collect { results.add(it) }
        }
        // advanceUntilIdle() is needed to process the initial emission from the StateFlow's upstream.
        advanceUntilIdle()

        // When: User logs in
        fakeAuthDataSource.setUserState(UserStateInfo(isUserLoggedIn = true))
        advanceUntilIdle()

        // When: User logs out
        fakeAuthDataSource.setUserState(UserStateInfo(isUserLoggedIn = false))
        advanceUntilIdle()

        // Then: Expect 4 states: Loading -> initial false -> true -> false
        assertThat(results.size, `is`(4))
        assertThat(results[0], `is`(instanceOf(AuthDataResult.Loading::class.java)))
        assertThat((results[1] as AuthDataResult.Success).data, `is`(false))
        assertThat((results[2] as AuthDataResult.Success).data, `is`(true))
        assertThat((results[3] as AuthDataResult.Success).data, `is`(false))

        job.cancel()
    }

    @Test
    fun `isAuthenticated - when source throws error - emits Error`() = runTest {
        // Given
        val results = mutableListOf<AuthDataResult<Boolean>>()
        val job = launch(testDispatcher) {
            viewModel.isAuthenticated.collect { results.add(it) }
        }
        // advanceUntilIdle() processes the initial Loading and Success(false) emissions.
        advanceUntilIdle()

        // When
        fakeAuthDataSource.setShouldReturnError(true)
        // Trigger a new, distinct emission to propagate the error.
        // Using a different value for a property ensures StateFlow emits.
        fakeAuthDataSource.setUserState(UserStateInfo(userName = "Error Trigger"))
        advanceUntilIdle()

        // Then: Expect 3 states: Loading -> initial false -> Error
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`(instanceOf(AuthDataResult.Loading::class.java)))
        assertThat((results[1] as AuthDataResult.Success).data, `is`(false))
        assertThat(results[2], `is`(instanceOf(AuthDataResult.Error::class.java)))

        job.cancel()
    }

    // endregion

    // region saveGoal Tests
    @Test
    fun `saveGoal - with positive value - updates data source`() = runTest {
        // Given
        val goalType = GoalType.STEP
        val goalValue = 10000

        // When
        viewModel.saveGoal(goalType, goalValue)
        advanceUntilIdle() // Ensure the coroutine in viewModelScope completes

        // Then
        val currentGoals = fakeActivityDataSource.getCurrentGoals().first()
        assertThat(currentGoals.stepGoal, `is`(goalValue))
    }

    @Test
    fun `saveGoal - with zero value - updates data source`() = runTest {
        // Given
        val goalType = GoalType.WATER
        val goalValue = 0

        // When
        viewModel.saveGoal(goalType, goalValue)
        advanceUntilIdle()

        // Then
        val currentGoals = fakeActivityDataSource.getCurrentGoals().first()
        assertThat(currentGoals.waterGlassGoal, `is`(goalValue))
    }
    // endregion


    // region saveUserStateInfo Tests
    @Test
    fun `saveUserStateInfo - with complete info - updates data source`() = runTest {
        // Given
        val newUserState = UserStateInfo(uid = "uid-1", isOnboarded = true, userName = "Complete User")

        // When
        viewModel.saveUserStateInfo(newUserState)
        advanceUntilIdle()

        // Then
        val savedState = fakeActivityDataSource.getCurrentUserState().first()
        assertThat(savedState, `is`(newUserState))
    }
    // endregion
    // region finalizeAuthentication Tests
    @Test
    fun `finalizeAuthentication - when user exists - returns and saves logged in state`() = runTest {
        // Given
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.uid } returns "firebase-uid"
        every { mockUser.displayName } returns "Firebase User"
        fakeAuthDataSource.setFirebaseUser(mockUser)

        // When
        val resultState = viewModel.finalizeAuthentication()
        advanceUntilIdle()

        // Then
        assertThat(resultState.isUserLoggedIn, `is`(true))
        assertThat(resultState.uid, `is`("firebase-uid"))
        assertThat(resultState.userName, `is`("Firebase User"))

        val savedState = fakeAuthDataSource.observeUserLoginState().first()
        assertThat(savedState, `is`(true))
    }


    @Test
    fun `finalizeAuthentication - when no user is logged in - returns logged out state`() = runTest {
        // Given
        fakeAuthDataSource.setFirebaseUser(null)
        fakeAuthDataSource.setUserState(UserStateInfo(isUserLoggedIn = false))

        // When
        val resultState = viewModel.finalizeAuthentication()
        advanceUntilIdle()

        // Then
        assertThat(resultState.isUserLoggedIn, `is`(false))
    }

    @Test
    fun `finalizeAuthentication - when source throws error - throws exception`() {
        // Given
        fakeAuthDataSource.setShouldReturnError(true)

        // When & Then
        assertThrows(Exception::class.java) {
            runTest {
                viewModel.finalizeAuthentication()
            }
        }
    }
    // endregion

    // region signOut Tests
    @Test
    fun `signOut - when user is authenticated - updates state to signed out`() = runTest {
        // Given: User is logged in
        fakeAuthDataSource.setUserState(UserStateInfo(isUserLoggedIn = true, uid = "test-uid"))

        // When
        viewModel.signOut()
        advanceUntilIdle()

        // Then
        val finalState = fakeAuthDataSource.observeUserLoginState().first()
        assertThat(finalState, `is`(false))
        val finalUserState = fakeActivityDataSource.getCurrentUserState().first()
        assertThat(finalUserState.uid, `is`(""))
    }
    // endregion

    // region signOutAndResetData Tests
    @Test
    fun `signOutAndResetData - when called - updates data source to reset state`() = runTest {
        // Given: User is logged in and has some data
        fakeAuthDataSource.setUserState(UserStateInfo(isUserLoggedIn = true, uid = "test-uid", isOnboarded = true))

        // When
        viewModel.signOutAndResetData()
        advanceUntilIdle()

        // Then
        val finalState = fakeActivityDataSource.getCurrentUserState().first()
        assertThat(finalState.isUserLoggedIn, `is`(false))
        assertThat(finalState.isOnboarded, `is`(false))
        assertThat(finalState.uid, `is`(""))
        assertThat(finalState.userName, `is`("Test User"))
    }
    // endregion
}