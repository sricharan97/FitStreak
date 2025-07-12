package com.apptimistiq.android.fitstreak.authentication.onboarding

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apptimistiq.android.fitstreak.MainCoroutineRule
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.TestApplication
import com.apptimistiq.android.fitstreak.data.FakeAuthDataSource
import com.apptimistiq.android.fitstreak.main.data.AuthDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Inject
    lateinit var authDataSource: AuthDataSource

    private lateinit var navController: TestNavHostController
    private lateinit var fakeAuthDataSource: FakeAuthDataSource

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Context>() as TestApplication
        (app.appComponent as com.apptimistiq.android.fitstreak.di.TestApplicationComponent).inject(this)

        fakeAuthDataSource = authDataSource as FakeAuthDataSource

        navController = TestNavHostController(app)
        navController.setGraph(R.navigation.nav_graph)
    }

    private fun launchLoginFragment(): FragmentScenario<LoginFragment> {
        return launchFragmentInContainer<LoginFragment>(
            themeResId = R.style.Theme_FitStreak
        ).onFragment { fragment ->
            fragment.shouldAutoLaunchSignIn = false
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun test_loginSuccess_forNewUser_navigatesToWelcome() = runTest {
        // Setup
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        fakeAuthDataSource.setFirebaseUser(mockUser)
        fakeAuthDataSource.setUserState(UserStateInfo(isUserLoggedIn = false, isOnboarded = false))
        navController.setCurrentDestination(R.id.loginFragment)

        // Launch fragment
        val scenario = launchLoginFragment()

        // Simulate successful authentication
        scenario.onFragment { fragment ->
            fragment.onSignInResult(FirebaseAuthUIAuthenticationResult(RESULT_OK, null))
        }

        // Wait for coroutines
        advanceUntilIdle()

        // Verify navigation
        assertEquals(R.id.welcomeFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_loginSuccess_forExistingUser_navigatesToHome() = runTest {
        // Setup
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        fakeAuthDataSource.setFirebaseUser(mockUser)
        fakeAuthDataSource.setUserState(UserStateInfo(isUserLoggedIn = false, isOnboarded = true))
        navController.setCurrentDestination(R.id.loginFragment)

        // Launch fragment
        val scenario = launchLoginFragment()

        // Simulate successful authentication
        scenario.onFragment { fragment ->
            fragment.onSignInResult(FirebaseAuthUIAuthenticationResult(RESULT_OK, null))
        }

        // Wait for coroutines
        advanceUntilIdle()

        // Verify navigation
        assertEquals(R.id.homeTransitionFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_loginCanceled_byUser_popsBackStack() = runTest {
        // Setup
        navController.setCurrentDestination(R.id.dashboardFragment)
        navController.navigate(R.id.loginFragment)

        // Launch fragment
        val scenario = launchLoginFragment()

        // Simulate canceled authentication
        scenario.onFragment { fragment ->
            fragment.onSignInResult(FirebaseAuthUIAuthenticationResult(999, null))
        }

        // Wait for coroutines
        advanceUntilIdle()

        // Verify navigation
        assertEquals(R.id.dashboardFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_loginFails_withError_popsBackStack() = runTest {
        // Setup
        navController.setCurrentDestination(R.id.dashboardFragment)
        navController.navigate(R.id.loginFragment)

        // Launch fragment
        val scenario = launchLoginFragment()

        // Simulate failed authentication
        scenario.onFragment { fragment ->
            fragment.onSignInResult(FirebaseAuthUIAuthenticationResult(123, null))
        }

        // Wait for coroutines
        advanceUntilIdle()

        // Verify navigation
        assertEquals(R.id.dashboardFragment, navController.currentDestination?.id)
    }


    @Test
    fun test_initialUiState_verifyFragmentDisplayed() = runTest {
        // Launch fragment but disable auto-signin
        val scenario = launchLoginFragment()

        // Verify fragment is in proper initial state (basic visibility check)
        scenario.onFragment { fragment ->
            assertEquals(View.VISIBLE, fragment.requireView().visibility)
            // Since LoginFragment has minimal UI (just a FrameLayout container),
            // we can just verify it exists and is visible
        }
    }







}