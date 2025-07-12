package com.apptimistiq.android.fitstreak.main.progressTrack

import com.apptimistiq.android.fitstreak.data.FakeActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ProgressViewModelTest {

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Subject under test
    private lateinit var viewModel: ProgressViewModel

    // Fake dependency
    private lateinit var fakeDataSource: FakeActivityDataSource

    @Before
    fun setup() {
        // Set the main dispatcher to the test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Initialize fakes and the ViewModel
        fakeDataSource = FakeActivityDataSource()
        viewModel = ProgressViewModel(fakeDataSource)
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher after tests
        Dispatchers.resetMain()
    }

    @Test
    fun `navigateToEditActivity - updates navigation state and current activity type`() = runTest {
        // Given
        val navResults = mutableListOf<ActivityType>()
        val job = launch(testDispatcher) {
            viewModel.navigateEditActivity.collect { navResults.add(it) }
        }
        advanceUntilIdle() // Collect initial value

        // When
        viewModel.navigateToEditActivity(ActivityType.WATER)
        viewModel.prepareForEditing(ActivityType.WATER)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.currentActivityType.value, `is`(ActivityType.WATER))
        assertThat(navResults.size, `is`(2))
        assertThat(navResults[0], `is`(ActivityType.DEFAULT))
        assertThat(navResults[1], `is`(ActivityType.WATER))

        // When completed
        viewModel.navigateToEditActivityCompleted()
        advanceUntilIdle()

        // Then
        assertThat(navResults.size, `is`(3))
        assertThat(navResults.last(), `is`(ActivityType.DEFAULT))

        job.cancel()
    }

    @Test
    fun `increment and decrement activity value - updates displayed value`() = runTest {
        // Given
        val results = mutableListOf<Int>()
        val job = launch(testDispatcher) {
            viewModel.displayedActivityValue.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial 0

        // When
        viewModel.updateDisplayedActivityVal(10)
        advanceUntilIdle()
        viewModel.incrementActivityValue()
        advanceUntilIdle()
        viewModel.decrementActivityValue()
        advanceUntilIdle()
        viewModel.decrementActivityValue()
        advanceUntilIdle()

        // Then
        assertThat(results, `is`(listOf(0, 10, 11, 10, 9)))

        job.cancel()
    }

    @Test
    fun `saveActivity - when no activity for today - calls saveActivity in data source`() = runTest {
        // Given
        fakeDataSource.setTodayActivity(null)
        advanceUntilIdle()

        viewModel.addLitres(5)
        viewModel.addSteps(5000)

        // When
        viewModel.saveActivity()
        advanceUntilIdle()

        // Then
        val savedActivity = fakeDataSource.getTodayActivity().first()
        assertThat(savedActivity?.find { it.dataType == ActivityType.WATER }?.currentReading, `is`(5))
        assertThat(savedActivity?.find { it.dataType == ActivityType.STEP }?.currentReading, `is`(5000))
        assertThat(viewModel.uiState.value.activitySavedForDay, `is`(true))
    }

    @Test
    fun `saveActivity - when activity for today exists - calls updateActivity in data source`() = runTest {
        // Given
        // A fresh ViewModel is needed to ensure its internal state is not polluted by other tests.
        // This should ideally be handled in a @Before method.
        val localViewModel = ProgressViewModel(fakeDataSource)

        // Set a known initial state. The test assertion expects 0 steps, so we set it to 0 here.
        // This makes the test independent of the default fake data or previous test states.
        val initialActivity = com.apptimistiq.android.fitstreak.main.data.database.Activity(
            dateOfActivity = System.currentTimeMillis(),
            steps = 0,
            waterGlasses = 0,
            sleepHours = 0,
            exerciseCalories = 0
        )
        fakeDataSource.setTodayActivity(initialActivity)
        advanceUntilIdle() // Allow flows to update

        // Add a different activity type to trigger an update.
        localViewModel.addLitres(5)

        // When
        localViewModel.saveActivity()
        advanceUntilIdle()

        // Then
        val updatedActivityItems = fakeDataSource.getTodayActivity().first()
        // Verify the new value was added
        assertThat(updatedActivityItems?.find { it.dataType == com.apptimistiq.android.fitstreak.main.data.domain.ActivityType.WATER }?.currentReading, `is`(5))
        // Verify the original step count (0) was preserved, which now matches the assertion.
        assertThat(updatedActivityItems?.find { it.dataType == com.apptimistiq.android.fitstreak.main.data.domain.ActivityType.STEP }?.currentReading, `is`(0))
    }

    @Test
    fun `updateUserActivityVal - for WATER - updates fit flow and navigates back`() = runTest {
        // Given
        // Ensure activityItemsToday is populated by collecting it.
        val activityCollectorJob = launch { viewModel.activityItemsToday.collect() }
        advanceUntilIdle()

        val navResults = mutableListOf<Boolean>()
        val fitWaterResults = mutableListOf<Int>()
        val navJob = launch { viewModel.navigateBackProgress.collect { navResults.add(it) } }
        val fitWaterJob = launch { viewModel.updateFitWater.collect { fitWaterResults.add(it) } }
        advanceUntilIdle() // Collect initial values

        // 1. Prepare for editing, which loads the initial value from the data source.
        viewModel.prepareForEditing(ActivityType.WATER)
        advanceUntilIdle()

        // 2. Simulate the user entering a new value.
        viewModel.updateDisplayedActivityVal(10)
        advanceUntilIdle()

        // When
        // 3. Save the new value.
        viewModel.updateUserActivityVal()
        advanceUntilIdle()

        // Then
        // Check navigation was triggered
        assertThat(navResults.last(), `is`(true))

        // Check the value was sent to the update flow
        assertThat(fitWaterResults.last(), `is`(10))

        // Cleanup
        activityCollectorJob.cancel()
        navJob.cancel()
        fitWaterJob.cancel()
    }

    @Test
    fun `updateUserActivityVal - for SLEEP - updates fit flow and navigates back`() = runTest {
        // Given
        // We must collect activityItemsToday to make the StateFlow active due to SharingStarted.WhileSubscribed.
        val activityCollectorJob = launch(testDispatcher) { viewModel.activityItemsToday.collect() }
        advanceUntilIdle() // Ensure the initial activity data is loaded.

        val fitResults = mutableListOf<Int>()
        val navResults = mutableListOf<Boolean>()
        val fitJob = launch(testDispatcher) { viewModel.updateFitSleep.collect { fitResults.add(it) } }
        val navJob = launch(testDispatcher) { viewModel.navigateBackProgress.collect { navResults.add(it) } }
        advanceUntilIdle() // Collect initial values

        // 1. Prepare for editing, which loads the initial value from the data source.
        viewModel.prepareForEditing(ActivityType.SLEEP)
        advanceUntilIdle()

        // 2. Simulate the user entering a new value.
        viewModel.updateDisplayedActivityVal(8)
        advanceUntilIdle()

        // When
        // 3. Save the new value.
        viewModel.updateUserActivityVal()
        advanceUntilIdle()

        // Then
        // Assert that the sleep value to be updated is correct
        assertThat(fitResults.last(), `is`(8))

        // Assert that navigation back is triggered
        assertThat(navResults.last(), `is`(true))

        // Cleanup
        activityCollectorJob.cancel()
        fitJob.cancel()
        navJob.cancel()
    }

    @Test
    fun `updateUserEnteredValues - updates data source correctly`() = runTest {
        // GIVEN
        // 1. Set initial activity data in the fake data source.
        val initialActivity = com.apptimistiq.android.fitstreak.main.data.database.Activity(
            id = 1,
            dateOfActivity = 1L,
            steps = 1000,
            exerciseCalories = 100,
            waterGlasses = 1,
            sleepHours = 1
        )
        fakeDataSource.setTodayActivity(initialActivity)

        // 2. Set the activity type to be edited.
        val activityTypeToEdit = com.apptimistiq.android.fitstreak.main.data.domain.ActivityType.EXERCISE
        viewModel.navigateToEditActivity(activityTypeToEdit)
        viewModel.prepareForEditing(activityTypeToEdit)

        // 3. Start collecting the activityItemsToday flow to make it active.
        val activityItemsCollector = launch(testDispatcher) { viewModel.activityItemsToday.collect() }
        advanceUntilIdle() // Ensure the flow from the data source is collected.

        // WHEN
        val newExerciseValue = 150
        viewModel.updateUserEnteredValues(newExerciseValue)
        advanceUntilIdle() // Allow the viewModelScope.launch in the function to complete.

        // THEN
        // Check that the data source was updated correctly.
        val updatedActivityItems = fakeDataSource.getTodayActivity().first()
       assertThat(updatedActivityItems, notNullValue())

        val exerciseItem = updatedActivityItems!!.find { it.dataType == activityTypeToEdit }
       assertThat(exerciseItem?.currentReading, `is`(newExerciseValue))

        // Verify other values were not changed.
        val stepsItem = updatedActivityItems.find { it.dataType == com.apptimistiq.android.fitstreak.main.data.domain.ActivityType.STEP }
       assertThat(stepsItem?.currentReading, `is`(initialActivity.steps))

        // Clean up the collector
        activityItemsCollector.cancel()
    }

    @Test
    fun `addSteps - updates UI state to reflect steps are read`() = runTest {
        // When
        viewModel.addSteps(8000)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.readSteps, `is`(true))
    }

    @Test
    fun `addCalories - updates UI state to reflect calories are read`() = runTest {
        // When
        viewModel.addCalories(500)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.readCalories, `is`(true))
    }

    @Test
    fun `addLitres - updates UI state to reflect water intake is read`() = runTest {
        // When
        viewModel.addLitres(2)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.readWaterLitres, `is`(true))
    }

    @Test
    fun `addSleepHrs - updates UI state to reflect sleep hours are read`() = runTest {
        // When
        viewModel.addSleepHrs(8)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.readSleepHrs, `is`(true))
    }

    @Test
    fun `saveActivity - when successful - updates UI state to indicate activity is saved`() = runTest {
        // Given
        viewModel.addSteps(5000)
        advanceUntilIdle()

        // When
        viewModel.saveActivity()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.activitySavedForDay, `is`(true))
    }


    @Test
    fun `navigateToEditActivityCompleted - resets navigation state`() = runTest {
        // Given
        val results = mutableListOf<ActivityType>()
        val job = launch(testDispatcher) {
            viewModel.navigateEditActivity.collect { results.add(it) }
        }
        viewModel.navigateToEditActivity(ActivityType.WATER)
        advanceUntilIdle()

        // When
        viewModel.navigateToEditActivityCompleted()
        advanceUntilIdle()

        // Then
        assertThat(results.last(), `is`(ActivityType.DEFAULT))

        job.cancel()
    }

    @Test
    fun `navigateBackToProgressFragmentCompleted - resets back navigation state`() = runTest {
        // Given
        val results = mutableListOf<Boolean>()
        val job = launch(testDispatcher) {
            viewModel.navigateBackProgress.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial false

        // When
        viewModel.navigateBackToProgressFragmentCompleted()
        advanceUntilIdle()

        // Then
        assertThat(results.last(), `is`(false))

        job.cancel()
    }

    @Test
    fun `updateDisplayedActivityVal - updates displayed value`() = runTest {
        // Given
        val newValue = 42
        val results = mutableListOf<Int>()
        val job = launch(testDispatcher) {
            viewModel.displayedActivityValue.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial 0

        // When
        viewModel.updateDisplayedActivityVal(newValue)
        advanceUntilIdle()

        // Then
        assertThat(results[0], `is`(0))
        assertThat(results[1], `is`(newValue))

        job.cancel()
    }

    @Test
    fun `incrementActivityValue - increases displayed value by 1`() = runTest {
        // Given
        val initialValue = 5
        viewModel.updateDisplayedActivityVal(initialValue)
        advanceUntilIdle()

        // When
        viewModel.incrementActivityValue()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.displayedActivityValue.value, `is`(initialValue + 1))
    }

    @Test
    fun `decrementActivityValue - decreases displayed value by 1`() = runTest {
        // Given
        val initialValue = 5
        viewModel.updateDisplayedActivityVal(initialValue)
        advanceUntilIdle()

        // When
        viewModel.decrementActivityValue()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.displayedActivityValue.value, `is`(initialValue - 1))
    }

    @Test
    fun `accessGoogleFit - updates UI state to allow Google Fit access`() = runTest {
        // Given
        assertThat(viewModel.uiState.value.canAccessGoogleFit, `is`(false))

        // When
        viewModel.accessGoogleFit()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.canAccessGoogleFit, `is`(true))
    }

    @Test
    fun `doneWithSubscription - updates UI state to mark subscription as done`() = runTest {
        // Given
        assertThat(viewModel.uiState.value.subscriptionDone, `is`(false))

        // When
        viewModel.doneWithSubscription()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.subscriptionDone, `is`(true))
    }

    @Test
    fun `fitWaterUpdated - resets water update tracking`() = runTest {
        // When
        viewModel.fitWaterUpdated()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.updateFitWater.value, `is`(0))
    }

    @Test
    fun `fitExerciseUpdated - resets exercise update tracking`() = runTest {
        // When
        viewModel.fitExerciseUpdated()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.updateFitExercise.value, `is`(0))
    }

    @Test
    fun `fitSleepUpdated - resets sleep update tracking`() = runTest {
        // When
        viewModel.fitSleepUpdated()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.updateFitSleep.value, `is`(0))
    }

    @Test
    fun `updateDisplayedActivityVal - with zero - updates state correctly`() = runTest {
        // Given
        val results = mutableListOf<Int>()
        val job = launch(testDispatcher) {
            viewModel.displayedActivityValue.collect { results.add(it) }
        }
        advanceUntilIdle() // Collect initial value

        // First update with a non-zero value to ensure a change
        viewModel.updateDisplayedActivityVal(5)
        advanceUntilIdle()

        // When - update to zero
        viewModel.updateDisplayedActivityVal(0)
        advanceUntilIdle()

        // Then
        assertThat(results.size, `is`(3)) // Initial, 5, and 0
        assertThat(results[0], `is`(0)) // Initial value
        assertThat(results[1], `is`(5)) // First update
        assertThat(results[2], `is`(0)) // Final update to zero

        job.cancel()
    }

    @Test
    fun `decrementActivityValue - when already at zero - stays at zero`() = runTest {
        // Given
        val results = mutableListOf<Int>()
        val job = launch(testDispatcher) {
            viewModel.displayedActivityValue.collect { results.add(it) }
        }
        advanceUntilIdle()
        assertThat(results[0], `is`(0)) // Initial value is zero

        // When
        viewModel.decrementActivityValue()
        advanceUntilIdle()

        // Then
        assertThat(results.size, `is`(1)) // StateFlow does not emit the same value twice
        assertThat(results.last(), `is`(0)) // Value should remain 0
        job.cancel()
    }


}