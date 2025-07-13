package com.apptimistiq.android.fitstreak.main.progressTrack

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import com.apptimistiq.android.fitstreak.TestApplication
import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.di.TestApplicationComponent
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.RecordingClient
import com.google.android.gms.fitness.SessionsClient
import com.google.android.gms.tasks.Tasks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Inject
import org.junit.Assert.assertEquals


@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class DailyProgressFragmentTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Inject
    lateinit var activityDataSource: ActivityDataSource

    private lateinit var navController: TestNavHostController
    private lateinit var fakeActivityDataSource: FakeActivityDataSource
    private lateinit var mockAccount: GoogleSignInAccount

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Context>() as TestApplication
        (app.appComponent as TestApplicationComponent).inject(this)

        fakeActivityDataSource = activityDataSource as FakeActivityDataSource

        navController = TestNavHostController(app)
        navController.setGraph(R.navigation.nav_graph)
        navController.setCurrentDestination(R.id.daily_progress_fragment)

        // Mock Google Fit dependencies
        mockkStatic(GoogleSignIn::class)
        mockAccount = mockk<GoogleSignInAccount>(relaxed = true)
        every { GoogleSignIn.getAccountForExtension(any(), any()) } returns mockAccount
        every { GoogleSignIn.hasPermissions(mockAccount, any<FitnessOptions>()) } returns true


        mockkStatic(Fitness::class)
        every { Fitness.getRecordingClient(any<Context>(), any()) } returns mockk(relaxed = true)
        val mockHistoryClient = mockk<HistoryClient>(relaxed = true)
        every { Fitness.getHistoryClient(any<Context>(), any()) } returns mockHistoryClient
        every { Fitness.getSessionsClient(any<Context>(), any()) } returns mockk(relaxed = true)

        val mockDataSet = mockk<com.google.android.gms.fitness.data.DataSet>(relaxed = true)
        every { mockHistoryClient.readDailyTotal(any()) } returns Tasks.forResult(mockDataSet)
    }

    @After
    fun tearDown() {
        fakeActivityDataSource.reset()
    }

    private fun launchDailyProgressFragment(): androidx.fragment.app.testing.FragmentScenario<DailyProgressFragment> {
        val scenario = launchFragmentInContainer<DailyProgressFragment>(
            themeResId = R.style.Theme_FitStreak
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        return scenario
    }

    @Test
    fun test_initialUIState_displaysCorrectElements() = runTest {
        // Given a fresh launch of the fragment
        launchDailyProgressFragment()

        // Then the correct UI elements are displayed
        onView(withId(R.id.progress_coordinator_layout_root)).check(matches(isDisplayed()))
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun test_activityData_isDisplayedInRecyclerView() = runTest {
        // Given the fragment is launched
        launchDailyProgressFragment()

        // Then the activity data is displayed
        onView(withText("STEP")).check(matches(isDisplayed()))
        onView(withText("WATER")).check(matches(isDisplayed()))
        onView(withText("SLEEP")).check(matches(isDisplayed()))
        onView(withText("EXERCISE")).check(matches(isDisplayed()))
    }

    @Test
    fun test_clickOnActivityItem_navigatesToEditActivity() = runTest {
        // Given the fragment is launched
        launchDailyProgressFragment()

        // When a clickable activity item is clicked (e.g., Water)
        onView(withText("WATER")).perform(click())

        // Then it navigates to the EditActivityFragment
        assertEquals(R.id.editActivityFragment, navController.currentDestination?.id)
    }

    @Test
    fun test_clickOnStepsActivityItem_showsSnackbar() = runTest {
        // Given the fragment is launched
        launchDailyProgressFragment()

        // When the "Steps" item is clicked
        onView(withText("STEP")).perform(click())

        // Then a snackbar is shown
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("Your steps are auto detected")))
    }

    @Test
    fun test_fragmentRecreation_maintainsState() = runTest {
        // Given the fragment is launched
        val scenario = launchDailyProgressFragment()
        advanceUntilIdle()

        // When the fragment is recreated
        scenario.recreate()
        advanceUntilIdle()

        // Then the activity data is still displayed
        onView(withText("STEP")).check(matches(isDisplayed()))
        onView(withText("WATER")).check(matches(isDisplayed()))
    }

    @Test
    fun test_errorState_showsErrorMessage() = runTest {
        // Given the data source is set to return an error
        fakeActivityDataSource.setShouldReturnError(true)

        // When the fragment is launched
        launchDailyProgressFragment()
        advanceUntilIdle()

        // Then a snackbar with the error message is shown
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("Test exception")))
    }

    @Test
    fun test_activityDataValues_areDisplayedCorrectly() = runTest {
        // Given specific activity data is set
        val todayActivity = com.apptimistiq.android.fitstreak.main.data.database.Activity(
            id = 1,
            dateOfActivity = System.currentTimeMillis(),
            steps = 5000,
            waterGlasses = 5,
            sleepHours = 6,
            exerciseCalories = 250
        )
        fakeActivityDataSource.setTodayActivity(todayActivity)
        advanceUntilIdle()

        // When the fragment is launched
        launchDailyProgressFragment()
        advanceUntilIdle()

        // Then the correct values are displayed in the RecyclerView
        onView(withText("5000")).check(matches(isDisplayed()))
        onView(withText("5")).check(matches(isDisplayed()))
        onView(withText("6")).check(matches(isDisplayed()))
        onView(withText("250")).check(matches(isDisplayed()))
    }

    @Test
    fun test_permissionDenied_showsSnackbarMessage() = runTest {
        // Given that Google Fit permissions have not been granted
        every { GoogleSignIn.hasPermissions(mockAccount, any<FitnessOptions>()) } returns false

        // When the fragment is launched
        val scenario = launchDailyProgressFragment()
        advanceUntilIdle()

        // And we manually simulate the permission being denied
        scenario.onFragment { fragment ->
            fragment.onActivityResult(101, android.app.Activity.RESULT_CANCELED, null)
        }
        advanceUntilIdle()

        // Then a snackbar is shown with the appropriate message
        val expectedMessage = "This app needs permissions to show the activities"
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(expectedMessage)))
    }


}
