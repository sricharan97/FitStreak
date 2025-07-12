package com.apptimistiq.android.fitstreak.authentication.onboarding

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apptimistiq.android.fitstreak.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeFragmentTest {

    @Test
    fun test_UI_elements_are_displayed() {
        // Launch the WelcomeFragment in a test container
        launchFragmentInContainer<WelcomeFragment>(themeResId = R.style.Theme_FitStreak)

        // Check if the welcome image is displayed
        onView(withId(R.id.welcome_image)).check(matches(isDisplayed()))

        // Check if the welcome text is displayed
        onView(withId(R.id.welcome_text)).check(matches(isDisplayed()))

        // Check if the navigate next button is displayed
        onView(withId(R.id.navigate_next_button)).check(matches(isDisplayed()))
    }

    @Test
    fun test_navigation_to_GoalSelectionFragment() {
        // Create a TestNavHostController
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        // Launch the WelcomeFragment in a test container
        val scenario = launchFragmentInContainer<WelcomeFragment>(themeResId = R.style.Theme_FitStreak)

        scenario.onFragment { fragment ->
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.nav_graph)

            // Set the current destination to WelcomeFragment to test navigation
            navController.setCurrentDestination(R.id.welcomeFragment)

            // Set the NavController on the fragment
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Perform a click on the navigate next button
        onView(withId(R.id.navigate_next_button)).perform(click())

        // Verify that the current destination is the GoalSelectionFragment
        assertEquals(R.id.goalSelectionFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_UI_elements_are_displayed_after_rotation() {
        // Launch the WelcomeFragment in a test container
        val scenario = launchFragmentInContainer<WelcomeFragment>(themeResId = R.style.Theme_FitStreak)

        // Recreate the fragment to simulate a configuration change
        scenario.recreate()

        // Check if the welcome image is displayed
        onView(withId(R.id.welcome_image)).check(matches(isDisplayed()))

        // Check if the welcome text is displayed
        onView(withId(R.id.welcome_text)).check(matches(isDisplayed()))

        // Check if the navigate next button is displayed
        onView(withId(R.id.navigate_next_button)).check(matches(isDisplayed()))
    }
}

