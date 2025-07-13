package com.apptimistiq.android.fitstreak.authentication.onboarding

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apptimistiq.android.fitstreak.MainCoroutineRule
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.TestApplication
import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class GoalSelectionFragmentTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Inject
    lateinit var activityDataSource: ActivityDataSource

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Context>() as TestApplication
        (app.appComponent as com.apptimistiq.android.fitstreak.di.TestApplicationComponent).inject(this)

        navController = TestNavHostController(app)
        navController.setGraph(R.navigation.nav_graph)
        navController.setCurrentDestination(R.id.goalSelectionFragment)
    }

    private fun setNumberPickerValue(value: Int): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController, view: View) {
                (view as NumberPicker).value = value
            }

            override fun getDescription(): String {
                return "Set the value of a NumberPicker"
            }

            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(NumberPicker::class.java)
            }
        }
    }

    @Test
    fun clickDoneButton_savesGoalsAndNavigates() = runTest {
        (activityDataSource as FakeActivityDataSource).setCurrentUserState(
            UserStateInfo(isUserLoggedIn = true, isOnboarded = false)
        )

        val scenario = launchFragmentInContainer<GoalSelectionFragment>(
            Bundle(),
            R.style.Theme_FitStreak
        )

        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(ViewMatchers.withId(R.id.step_count_picker)).perform(setNumberPickerValue(0))
        onView(ViewMatchers.withId(R.id.water_glass_picker)).perform(setNumberPickerValue(5))
        onView(ViewMatchers.withId(R.id.sleep_hour_picker)).perform(setNumberPickerValue(6))
        onView(ViewMatchers.withId(R.id.exercise_cal_picker)).perform(setNumberPickerValue(2))

        onView(ViewMatchers.withId(R.id.goal_selection_done_button)).perform(click())

        advanceUntilIdle()

        val savedGoals = activityDataSource.getCurrentGoals().first()
        assertEquals(1000, savedGoals.stepGoal)
        assertEquals(5, savedGoals.waterGlassGoal)
        assertEquals(6, savedGoals.sleepGoal)
        assertEquals(150, savedGoals.exerciseGoal)

        val finalState = activityDataSource.getCurrentUserState().first()
        assertEquals(true, finalState.isOnboarded)

        assertEquals(R.id.homeTransitionFragment, navController.currentDestination?.id)
    }

    @Test
    fun clickDoneButton_withDefaultValues_savesDefaultGoalsAndNavigates() = runTest {
        (activityDataSource as FakeActivityDataSource).setCurrentUserState(
            UserStateInfo(isUserLoggedIn = true, isOnboarded = false)
        )

        val scenario = launchFragmentInContainer<GoalSelectionFragment>(
            Bundle(),
            R.style.Theme_FitStreak
        )

        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(ViewMatchers.withId(R.id.goal_selection_done_button)).perform(click())

        advanceUntilIdle()

        val savedGoals = activityDataSource.getCurrentGoals().first()
        assertEquals(1000, savedGoals.stepGoal)
        assertEquals(0, savedGoals.waterGlassGoal)
        assertEquals(0, savedGoals.sleepGoal)
        assertEquals(50, savedGoals.exerciseGoal)

        val finalState = activityDataSource.getCurrentUserState().first()
        assertEquals(true, finalState.isOnboarded)

        assertEquals(R.id.homeTransitionFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_immediateNavigation_whenUserIsAlreadyOnboarded() = runTest {
        (activityDataSource as FakeActivityDataSource).setCurrentUserState(
            UserStateInfo(isUserLoggedIn = true, isOnboarded = false)
        )

        launchFragmentInContainer<GoalSelectionFragment>(
            Bundle(),
            R.style.Theme_FitStreak
        ).onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        (activityDataSource as FakeActivityDataSource).setCurrentUserState(
            UserStateInfo(isUserLoggedIn = true, isOnboarded = true)
        )

        advanceUntilIdle()

        assertEquals(R.id.homeTransitionFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_noNavigation_whenUserIsNotLoggedIn() = runTest {
        (activityDataSource as FakeActivityDataSource).setCurrentUserState(
            UserStateInfo(isUserLoggedIn = false, isOnboarded = false)
        )

        launchFragmentInContainer<GoalSelectionFragment>(
            Bundle(),
            R.style.Theme_FitStreak
        ).onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(ViewMatchers.withId(R.id.goal_selection_done_button)).perform(click())

        advanceUntilIdle()

        assertNotEquals(R.id.homeTransitionFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_noStateChangeOrNavigation_whenDataSourceReturnsError() = runTest {
        val fakeDataSource = (activityDataSource as FakeActivityDataSource)
        val initialState = UserStateInfo(isUserLoggedIn = true, isOnboarded = false)
        fakeDataSource.setCurrentUserState(initialState)

        launchFragmentInContainer<GoalSelectionFragment>(
            Bundle(),
            R.style.Theme_FitStreak
        ).onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        fakeDataSource.setShouldReturnError(true)

        onView(ViewMatchers.withId(R.id.goal_selection_done_button)).perform(click())

        advanceUntilIdle()

        fakeDataSource.setShouldReturnError(false)
        val finalState = fakeDataSource.getCurrentUserState().first()

        assertEquals(false, finalState.isOnboarded)
        assertNotEquals(R.id.homeTransitionFragment, navController.currentDestination?.id)
    }
}