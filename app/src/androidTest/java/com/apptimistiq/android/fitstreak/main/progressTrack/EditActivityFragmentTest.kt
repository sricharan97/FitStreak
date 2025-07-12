package com.apptimistiq.android.fitstreak.main.progressTrack

import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.apptimistiq.android.fitstreak.MainCoroutineRule
import com.apptimistiq.android.fitstreak.R
import com.apptimistiq.android.fitstreak.TestApplication
import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.di.TestApplicationComponent
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
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
class EditActivityFragmentTest(private val activityType: ActivityType) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(ActivityType.WATER),
                arrayOf(ActivityType.EXERCISE),
                arrayOf(ActivityType.SLEEP)
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
        val testComponent = app.appComponent as TestApplicationComponent
        testComponent.inject(this)
        fakeActivityDataSource = activityDataSource as FakeActivityDataSource

        navController = TestNavHostController(app)
        navController.setGraph(R.navigation.nav_graph)


    }

    private fun launchEditActivityFragment(): FragmentScenario<EditActivityFragment> {
        val bundle = Bundle().apply {
            putSerializable("act_type", activityType)
        }
        return launchFragmentInContainer<EditActivityFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_FitStreak
        ).onFragment { fragment ->
            navController.setCurrentDestination(R.id.editActivityFragment)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
    }

    @Test
    fun test_initialUIState_displaysCorrectElements() = runTest {
        // Given a fresh launch of the fragment
        launchEditActivityFragment()
        advanceUntilIdle()

        // Then the correct UI elements are displayed based on activity type
        if (activityType == ActivityType.EXERCISE) {
            onView(withId(R.id.add_workout_edit_title)).check(matches(isDisplayed()))
            onView(withId(R.id.calories_text)).check(matches(isDisplayed()))
            onView(withId(R.id.edit_goal_set_button)).check(matches(isDisplayed()))
        } else {
            onView(withId(R.id.activity_edit_type)).check(matches(isDisplayed()))
            onView(withId(R.id.activity_value_seg)).check(matches(isDisplayed()))
            onView(withId(R.id.activity_val_tag)).check(matches(isDisplayed()))
            onView(withId(R.id.edit_goal_set_button)).check(matches(isDisplayed()))
            onView(withId(R.id.activity_val_increment)).check(matches(isDisplayed()))
            onView(withId(R.id.activity_val_decrement)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun test_incrementButton_updatesValue() = runTest {
        if (activityType == ActivityType.EXERCISE) return@runTest

        // Given the fragment is launched
        launchEditActivityFragment()
        advanceUntilIdle()

        val initialValue = fakeActivityDataSource.getCurrentActivityVal(activityType).first()

        // When the increment button is clicked
        onView(withId(R.id.activity_val_increment)).perform(click())
        advanceUntilIdle()

        // Then the displayed value is updated
        val expectedValue = initialValue + 1
        onView(withId(R.id.activity_value)).check(matches(withText(expectedValue.toString())))
    }

    @Test
    fun test_decrementButton_updatesValue() = runTest {
        if (activityType == ActivityType.EXERCISE) return@runTest
        // Given the fragment is launched
        launchEditActivityFragment()
        advanceUntilIdle()

        val initialValue = fakeActivityDataSource.getCurrentActivityVal(activityType).first()

        // When the decrement button is clicked
        onView(withId(R.id.activity_val_decrement)).perform(click())
        advanceUntilIdle()

        // Then the displayed value is updated
        val expectedValue = (initialValue - 1).coerceAtLeast(0)
        onView(withId(R.id.activity_value)).check(matches(withText(expectedValue.toString())))
    }

    @Test
    fun test_saveButton_navigatesToHomeDestination() = runTest {
        // Given the fragment is launched
        launchEditActivityFragment()
        advanceUntilIdle()

        // When the save button is clicked
        onView(withId(R.id.edit_goal_set_button)).perform(click())
        advanceUntilIdle()

        // Then the app navigates back to the home destination
        assertEquals(R.id.daily_progress_fragment, navController.currentDestination?.id)
    }

    @Test
    fun test_fragmentRecreation_maintainsState() = runTest {
        if (activityType == ActivityType.EXERCISE) return@runTest
        // Given the fragment is launched and a value is incremented
        val scenario = launchEditActivityFragment()
        advanceUntilIdle()
        val initialValue = fakeActivityDataSource.getCurrentActivityVal(activityType).first()
        onView(withId(R.id.activity_val_increment)).perform(click())
        advanceUntilIdle()


        val expectedValue = initialValue+1


        // When the fragment is recreated
        scenario.recreate()
        advanceUntilIdle()

        // Then the displayed value is maintained
        onView(withId(R.id.activity_value)).check(matches(withText(expectedValue.toString())))
    }
}