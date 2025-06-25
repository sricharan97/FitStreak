package com.apptimistiq.android.fitstreak.main.dashboard

import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Subject under test
    private lateinit var viewModel: DashboardViewModel

    // Fake dependency
    private lateinit var fakeDataSource: FakeActivityDataSource

    @Before
    fun setup() {
        // Set the main dispatcher to the test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Initialize fakes and the ViewModel
        fakeDataSource = FakeActivityDataSource()
        viewModel = DashboardViewModel(fakeDataSource)
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher after tests
        Dispatchers.resetMain()
    }

    @Test
    fun `goals - when source emits - emits updated goal preferences`() = runTest {
        // Given
        val newGoals = GoalPreferences(stepGoal = 8000, waterGlassGoal = 10, sleepGoal = 7, exerciseGoal = 2500)
        val results = mutableListOf<GoalPreferences?>()
        val job = launch(testDispatcher) {
            viewModel.goals.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial null and value from data source

        // When
        fakeDataSource.setCurrentGoalPreferences(newGoals)
        advanceUntilIdle() // Allow the collector to receive the new state

        // Then
        // The flow emits: initialValue (null) -> value from dataSource -> new value from test
        val defaultGoals = GoalPreferences(stepGoal = 10000, waterGlassGoal = 8, sleepGoal = 8, exerciseGoal = 3000)
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`(nullValue()))
        assertThat(results[1], `is`(defaultGoals))
        assertThat(results[2], `is`(newGoals))

        job.cancel()
    }

    @Test
    fun `userInitialsState - when user name is updated - emits correct initials`() = runTest {
        // Given
        val results = mutableListOf<String>()
        val job = launch(testDispatcher) {
            viewModel.userInitialsState.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial "NA" and value from data source "TU"

        // When
        fakeDataSource.setCurrentUserState(UserStateInfo(userName = "John Doe"))
        advanceUntilIdle()

        // Then
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`("NA")) // Initial value from stateIn
        assertThat(results[1], `is`("TU")) // Initial from FakeDataSource
        assertThat(results[2], `is`("JD")) // New value

        job.cancel()
    }

    @Test
    fun `userInitialsState - when user name is blank - emits NA`() = runTest {
        // Given
        val results = mutableListOf<String>()
        val job = launch(testDispatcher) {
            viewModel.userInitialsState.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial "NA" and value from data source "TU"

        // When
        fakeDataSource.setCurrentUserState(UserStateInfo(userName = " "))
        advanceUntilIdle()

        // Then
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`("NA"))
        assertThat(results[1], `is`("TU"))
        assertThat(results[2], `is`("NA"))

        job.cancel()
    }

    @Test
    fun `init - fetches weekly activities and updates state`() = runTest {
        // This test validates the behavior of the 'init' block.
        // A new ViewModel is created here to ensure we test the initialization logic cleanly.
        // Given a list of activities
        val activities = listOf(Activity(id = 1, dateOfActivity = 1L, steps = 100, waterGlasses = 0, sleepHours = 0, exerciseCalories = 0))
        fakeDataSource.setWeekActivities(activities)

        advanceUntilIdle()

        // Then loading state is handled and activities are loaded
        assertThat(viewModel.isLoading.value, `is`(false))
        assertThat(viewModel.weeklyActivities.value, `is`(activities))
        assertThat(viewModel.error.value, `is`(null as String?))
    }

    @Test
    fun `init - when fetching activities fails - updates error state`() = runTest {
        // Given
        fakeDataSource.setShouldReturnError(true)

        // When
        // Initialize the ViewModel here, AFTER setting the error condition.
        viewModel = DashboardViewModel(fakeDataSource)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.isLoading.value, `is`(false))
        assertThat(viewModel.error.value, `is`(notNullValue()))
        assertThat(viewModel.weeklyActivities.value.isEmpty(), `is`(true))
    }

    @Test
    fun `navigateEditGoal - updates navigation state`() = runTest {
        // Given
        val results = mutableListOf<GoalUserInfo>()
        val job = launch(testDispatcher) {
            viewModel.navigateToEditGoal.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial value

        // When
        viewModel.navigateEditGoal(GoalUserInfo.STEPS)
        advanceUntilIdle() // Ensure the collector processes the navigation event
        viewModel.navigateToEditGoalCompleted()
        advanceUntilIdle() // Ensure the collector processes the completion event

        // Then
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`(GoalUserInfo.DEFAULT))
        assertThat(results[1], `is`(GoalUserInfo.STEPS))
        assertThat(results[2], `is`(GoalUserInfo.DEFAULT))

        job.cancel()
    }

    @Test
    fun `incrementGoalInfoValue - for STEPS - increments by 500`() = runTest {
        // Given
        val results = mutableListOf<Int>()
        val job = launch(testDispatcher) {
            viewModel.displayedGoalValue.collect { results.add(it) }
        }
        viewModel.navigateEditGoal(GoalUserInfo.STEPS)
        advanceUntilIdle()

        // When
        viewModel.updateDisplayedGoalInfoVal(1000)
        advanceUntilIdle()
        viewModel.incrementGoalInfoValue()
        advanceUntilIdle()

        // Then
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`(0)) // Initial value
        assertThat(results[1], `is`(1000))
        assertThat(results[2], `is`(1500))

        job.cancel()
    }

    @Test
    fun `decrementGoalInfoValue - for EXERCISE - decrements by 50`() = runTest {
        // Given
        val results = mutableListOf<Int>()
        val job = launch(testDispatcher) {
            viewModel.displayedGoalValue.collect { results.add(it) }
        }
        viewModel.navigateEditGoal(GoalUserInfo.EXERCISE)
        advanceUntilIdle()

        // When
        viewModel.updateDisplayedGoalInfoVal(200)
        advanceUntilIdle()
        viewModel.decrementGoalInfoValue()
        advanceUntilIdle()

        // Then
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`(0))
        assertThat(results[1], `is`(200))
        assertThat(results[2], `is`(150))

        job.cancel()
    }

    @Test
    fun `decrementGoalInfoValue - does not go below zero`() = runTest {
        // Given
        val results = mutableListOf<Int>()
        val job = launch(testDispatcher) {
            viewModel.displayedGoalValue.collect { results.add(it) }
        }
        viewModel.navigateEditGoal(GoalUserInfo.WEIGHT)
        advanceUntilIdle()
        viewModel.updateDisplayedGoalInfoVal(1)
        advanceUntilIdle() // results: [0, 1]

        // When
        viewModel.decrementGoalInfoValue() // value becomes 0
        advanceUntilIdle() // results: [0, 1, 0]
        viewModel.decrementGoalInfoValue() // value stays 0, no new emission
        advanceUntilIdle()

        // Then
        assertThat(results.size, `is`(3))
        assertThat(results.last(), `is`(0))

        job.cancel()
    }

    @Test
    fun `saveGoalInfo - saves data and triggers navigation back`() = runTest {
        // Given
        val navResults = mutableListOf<Boolean>()
        val job = launch(testDispatcher) {
            viewModel.navigateBackToDashboard.collect { navResults.add(it) }
        }
        viewModel.navigateEditGoal(GoalUserInfo.WEIGHT)
        advanceUntilIdle()
        viewModel.updateDisplayedGoalInfoVal(75)
        advanceUntilIdle() // Collects initial `false`

        // When
        viewModel.saveGoalInfo()
        advanceUntilIdle()

        // Then
        // Check that data was saved
        val savedWeight = fakeDataSource.getCurrentGoalUserInfo(GoalUserInfo.WEIGHT).first()
        assertThat(savedWeight, `is`(75))

        // Check navigation was triggered
        assertThat(navResults.size, `is`(2))
        assertThat(navResults[0], `is`(false))
        assertThat(navResults[1], `is`(true))

        job.cancel()
    }

    @Test
    fun `getChartData - returns correctly formatted data for last 7 days`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        val todaySeconds = TimeUnit.MILLISECONDS.toSeconds(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgoSeconds = TimeUnit.MILLISECONDS.toSeconds(calendar.timeInMillis)

        val activities = listOf(
            Activity(id = 1, dateOfActivity = todaySeconds, steps = 5000, waterGlasses = 8, exerciseCalories = 300, sleepHours = 7),
            Activity(id = 2, dateOfActivity = twoDaysAgoSeconds, steps = 2000, waterGlasses = 4, exerciseCalories = 100, sleepHours = 5)
        )
        // The viewModel's init block already started collecting weekly activities.
        // We update the source and wait for the viewModel to process it.
        fakeDataSource.setWeekActivities(activities)
        advanceUntilIdle()

        // When
        val stepsData = viewModel.getStepsData()

        // Then
        assertThat(stepsData.size, `is`(7))
        assertThat(stepsData.last().second, `is`(5000f)) // Today's value
        assertThat(stepsData[4].second, `is`(2000f)) // Two days ago value
        assertThat(stepsData[5].second, `is`(0f)) // Yesterday's value (no data)
    }

    @Test
    fun `getInitialsFromName - returns correct initials for various names`() {
        assertThat(viewModel.getInitialsFromName("Sricharan"), `is`("S"))
        assertThat(viewModel.getInitialsFromName("Sricharan Reddy"), `is`("SR"))
        assertThat(viewModel.getInitialsFromName("  Sricharan   Reddy  "), `is`("SR"))
        assertThat(viewModel.getInitialsFromName(""), `is`("NA"))
        assertThat(viewModel.getInitialsFromName("   "), `is`("NA"))
    }
}