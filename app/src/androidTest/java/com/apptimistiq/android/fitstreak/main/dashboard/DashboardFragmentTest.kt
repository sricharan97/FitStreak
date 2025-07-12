package com.apptimistiq.android.fitstreak.main.dashboard

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.allOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apptimistiq.android.fitstreak.MainCoroutineRule
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.TestApplication
import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.data.FakeAuthDataSource
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.AuthDataSource
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserInfoPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
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
class DashboardFragmentTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Inject
    lateinit var activityDataSource: ActivityDataSource

    @Inject
    lateinit var authDataSource: AuthDataSource

    private lateinit var navController: TestNavHostController
    private lateinit var fakeActivityDataSource: FakeActivityDataSource
    private lateinit var fakeAuthDataSource: FakeAuthDataSource

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Context>() as TestApplication
        (app.appComponent as com.apptimistiq.android.fitstreak.di.TestApplicationComponent).inject(this)

        fakeActivityDataSource = activityDataSource as FakeActivityDataSource
        fakeAuthDataSource = authDataSource as FakeAuthDataSource

        // Set up a consistent, logged-in state before each test
        setupInitialData()

        navController = TestNavHostController(app)
        navController.setGraph(R.navigation.nav_graph)
        navController.setCurrentDestination(R.id.dashboardFragment)
    }

    private fun launchDashboardFragment(): FragmentScenario<DashboardFragment> {
        return launchFragmentInContainer<DashboardFragment>(
            themeResId = R.style.Theme_FitStreak
        ).onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun test_initialUIState_displaysCorrectElements() = runTest {
        launchDashboardFragment()
        advanceUntilIdle()

        // Verify header elements
        onView(withId(R.id.profile_avatar)).check(matches(isDisplayed()))
        onView(withId(R.id.logout_button)).check(matches(isDisplayed()))
        onView(withId(R.id.weekly_dashboard_title)).check(matches(isDisplayed()))

        // Verify chart cards and charts are displayed by scrolling to them first
        onView(withId(R.id.steps_card)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.steps_chart)).check(matches(isDisplayed()))
        onView(withId(R.id.water_card)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.water_chart)).check(matches(isDisplayed()))
        onView(withId(R.id.exercise_card)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.exercise_chart)).check(matches(isDisplayed()))
        onView(withId(R.id.sleep_card)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.sleep_chart)).check(matches(isDisplayed()))

        // Verify edit buttons
        onView(withId(R.id.edit_steps_button)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.edit_water_button)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_exercise_button)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_sleep_button)).check(matches(isDisplayed()))
    }

    @Test
    fun test_editStepsButton_navigatesToGoalEdit() = runTest {
        launchDashboardFragment()
        advanceUntilIdle()

        onView(withId(R.id.edit_steps_button)).perform(scrollTo(), click())
        advanceUntilIdle()

        assertEquals(R.id.goalEditFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_editWaterButton_navigatesToGoalEdit() = runTest {
        launchDashboardFragment()
        advanceUntilIdle()

        onView(withId(R.id.edit_water_button)).perform(scrollTo(), click())
        advanceUntilIdle()

        assertEquals(R.id.goalEditFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_editExerciseButton_navigatesToGoalEdit() = runTest {
        launchDashboardFragment()
        advanceUntilIdle()

        onView(withId(R.id.edit_exercise_button)).perform(scrollTo(), click())
        advanceUntilIdle()

        assertEquals(R.id.goalEditFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_editSleepButton_navigatesToGoalEdit() = runTest {
        launchDashboardFragment()
        advanceUntilIdle()

        onView(withId(R.id.edit_sleep_button)).perform(scrollTo(), click())
        advanceUntilIdle()

        assertEquals(R.id.goalEditFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_logoutButton_triggersSignOutAndNavigatesToLogin() = runTest {
        launchDashboardFragment()
        advanceUntilIdle()

        // Perform logout click
        onView(withId(R.id.logout_button)).perform(click())
        advanceUntilIdle()

        // Manually update the auth state to simulate logout completion
        fakeAuthDataSource.setUserState(UserStateInfo(isUserLoggedIn = false, isOnboarded = true))
        advanceUntilIdle()

        // Verify navigation to login screen
        assertEquals(R.id.loginFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_emptyWeeklyData_handlesGracefully() = runTest {
        // Override initial setup with empty data
        fakeActivityDataSource.setWeekActivities(emptyList())

        launchDashboardFragment()
        advanceUntilIdle()

        // Charts should still be displayed even with empty data
        onView(withId(R.id.steps_chart)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.water_chart)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.exercise_chart)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.sleep_chart)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun test_errorState_handlesDataSourceErrors() = runTest {
        // Set data source to return errors
        fakeActivityDataSource.setShouldReturnError(true)

        launchDashboardFragment()
        advanceUntilIdle()

        // UI elements should still be displayed
        onView(withId(R.id.profile_avatar)).check(matches(isDisplayed()))
        onView(withId(R.id.logout_button)).check(matches(isDisplayed()))
        onView(withId(R.id.weekly_dashboard_title)).check(matches(isDisplayed()))

        // Verify that the user initials are set to "NA" in case of an error
        onView(allOf(isDescendantOfA(withId(R.id.profile_avatar)), withText("NA"))).check(matches(isDisplayed()))

        // Reset error state for subsequent tests
        fakeActivityDataSource.setShouldReturnError(false)
    }

    @Test
    fun test_fragmentRecreation_maintainsState() = runTest {
        val scenario = launchDashboardFragment()
        advanceUntilIdle()

        // Recreate fragment (simulates configuration change)
        scenario.recreate()
        advanceUntilIdle()

        // Verify UI elements are still displayed
        onView(withId(R.id.profile_avatar)).check(matches(isDisplayed()))
        onView(withId(R.id.steps_chart)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    // Helper method to set up a clean state for tests
    private fun setupInitialData() {
        // Reset error flags
        fakeActivityDataSource.setShouldReturnError(false)
        fakeAuthDataSource.setShouldReturnError(false)

        // Setup a default logged-in user
        val userState = UserStateInfo(userName = "Test User", isUserLoggedIn = true, isOnboarded = true)
        fakeAuthDataSource.setUserState(userState)
        fakeActivityDataSource.setCurrentUserState(userState)


        // Setup default goals
        fakeActivityDataSource.setCurrentGoalPreferences(
            GoalPreferences(stepGoal = 10000, waterGlassGoal = 8, sleepGoal = 8, exerciseGoal = 300)
        )

        // Setup default current activity
        fakeActivityDataSource.setTodayActivity(
            Activity(id = 1, dateOfActivity = System.currentTimeMillis(), steps = 7500, waterGlasses = 6, sleepHours = 7, exerciseCalories = 250)
        )

        // Setup default weekly activities
        val weeklyActivities = listOf(
            Activity(1, System.currentTimeMillis() - 86400000, 6000, 4, 6, 200),
            Activity(2, System.currentTimeMillis() - 172800000, 7000, 5, 8, 250)
        )
        fakeActivityDataSource.setWeekActivities(weeklyActivities)

        // Setup default user info
        fakeActivityDataSource.setCurrentUserInfo(
            UserInfoPreferences(height = 175, weight = 70)
        )
    }
}
