package com.apptimistiq.android.fitstreak.main.dashboard

import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.apptimistiq.android.fitstreak.MainCoroutineRule
import com.apptimistiq.android.fitstreak.R
import org.hamcrest.CoreMatchers.not
import com.apptimistiq.android.fitstreak.TestApplication
import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import javax.inject.Inject

@ExperimentalCoroutinesApi
@RunWith(Parameterized::class)
class GoalEditFragmentTest(private val goalUserInfo: GoalUserInfo) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(GoalUserInfo.STEPS),
                arrayOf(GoalUserInfo.WATER),
                arrayOf(GoalUserInfo.EXERCISE),
                arrayOf(GoalUserInfo.SLEEP),
                arrayOf(GoalUserInfo.HEIGHT),
                arrayOf(GoalUserInfo.WEIGHT)
            )
        }
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Inject
    lateinit var activityDataSource: ActivityDataSource

    private lateinit var navController: TestNavHostController
    private lateinit var fakeActivityDataSource: FakeActivityDataSource

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Context>() as TestApplication
        (app.appComponent as com.apptimistiq.android.fitstreak.di.TestApplicationComponent).inject(this)

        fakeActivityDataSource = activityDataSource as FakeActivityDataSource

        navController = TestNavHostController(app)
        navController.setGraph(R.navigation.nav_graph)
        navController.setCurrentDestination(R.id.goalEditFragment)
    }

    private fun launchGoalEditFragment(): FragmentScenario<GoalEditFragment> {
        val bundle = Bundle().apply {
            putSerializable("info_type", goalUserInfo)
        }
        return launchFragmentInContainer<GoalEditFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_FitStreak
        ).onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun test_initialUIState_displaysCorrectElements() = runTest {
        // Given a fresh launch of the fragment
        launchGoalEditFragment()
        advanceUntilIdle()

        // Then the correct UI elements are displayed
        onView(withId(R.id.edit_type)).check(matches(isDisplayed()))
        if (goalUserInfo == GoalUserInfo.HEIGHT || goalUserInfo == GoalUserInfo.WEIGHT) {
            onView(withId(R.id.edit_type_descp)).check(matches(not(isDisplayed())))
        } else {
            onView(withId(R.id.edit_type_descp)).check(matches(isDisplayed()))
        }
        onView(withId(R.id.goal_tag)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_goal_set_button)).check(matches(isDisplayed()))
        onView(withId(R.id.goal_increment)).check(matches(isDisplayed()))
        onView(withId(R.id.goal_decrement)).check(matches(isDisplayed()))
    }

    @Test
    fun test_incrementButton_updatesValue() = runTest {
        // Given the fragment is launched
        launchGoalEditFragment()
        advanceUntilIdle()

        // When the increment button is clicked
        onView(withId(R.id.goal_increment)).perform(click())
        advanceUntilIdle()

        // Then the displayed value is updated
        val expectedValue = fakeActivityDataSource.getCurrentGoalUserInfo(goalUserInfo).first() + getIncrementValue(goalUserInfo)
        onView(withId(R.id.goal_value)).check(matches(withText(expectedValue.toString())))
    }

    @Test
    fun test_decrementButton_updatesValue() = runTest {
        // Given the fragment is launched
        launchGoalEditFragment()
        advanceUntilIdle()

        // When the decrement button is clicked
        onView(withId(R.id.goal_decrement)).perform(click())
        advanceUntilIdle()

        // Then the displayed value is updated
        val expectedValue = (fakeActivityDataSource.getCurrentGoalUserInfo(goalUserInfo).first() - getIncrementValue(goalUserInfo)).coerceAtLeast(0)
        onView(withId(R.id.goal_value)).check(matches(withText(expectedValue.toString())))
    }

    @Test
    fun test_saveButton_navigatesToDashboard() = runTest {
        // Given the fragment is launched
        launchGoalEditFragment()
        advanceUntilIdle()

        // When the save button is clicked
        onView(withId(R.id.edit_goal_set_button)).perform(click())
        advanceUntilIdle()

        // Then the app navigates back to the dashboard
        assertEquals(R.id.dashboardFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_fragmentRecreation_maintainsState() = runTest {
        // Given the fragment is launched and a value is incremented
        val scenario = launchGoalEditFragment()
        advanceUntilIdle()
        onView(withId(R.id.goal_increment)).perform(click())
        advanceUntilIdle()
        val expectedValue = fakeActivityDataSource.getCurrentGoalUserInfo(goalUserInfo).first() + getIncrementValue(goalUserInfo)

        // When the fragment is recreated
        scenario.recreate()
        advanceUntilIdle()

        // Then the displayed value is maintained
        onView(withId(R.id.goal_value)).check(matches(withText(expectedValue.toString())))
    }

    private fun getIncrementValue(goalUserInfo: GoalUserInfo): Int {
        return when (goalUserInfo) {
            GoalUserInfo.STEPS -> 500
            GoalUserInfo.EXERCISE -> 50
            else -> 1
        }
    }
}
