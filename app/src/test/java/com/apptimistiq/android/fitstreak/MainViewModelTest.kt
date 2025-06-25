package com.apptimistiq.android.fitstreak

import com.apptimistiq.android.fitstreak.main.MainViewModel
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
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handlePermissionResult - when granted - updates state correctly`() = runTest {
        // Given initial state
        val initialState = viewModel.uiState.first()

        // When permission is granted
        viewModel.handlePermissionResult(true)
        advanceUntilIdle()

        // Then UI state is updated accordingly
        val state = viewModel.uiState.first()
        assertThat(state.navigateToDailyProgress, `is`(true))
        assertThat(state.upgradeHomeFunctionality, `is`(true))
        assertThat(state.degradeHomeFunctionality, `is`(false))
        assertThat(state.isActivityPermissionDenied, `is`(false))
        assertThat(state.isHomeScreenDegraded, `is`(false))
        assertThat(state.showPermissionDeniedMessage, `is`(false))
    }

    @Test
    fun `handlePermissionResult - when denied - updates state correctly`() = runTest {
        // Given initial state
        val initialState = viewModel.uiState.first()

        // When permission is denied
        viewModel.handlePermissionResult(false)
        advanceUntilIdle()

        // Then UI state is updated accordingly
        val state = viewModel.uiState.first()
        assertThat(state.navigateToDailyProgress, `is`(false))
        assertThat(state.upgradeHomeFunctionality, `is`(false))
        assertThat(state.degradeHomeFunctionality, `is`(true))
        assertThat(state.isActivityPermissionDenied, `is`(true))
        assertThat(state.isHomeScreenDegraded, `is`(true))
        assertThat(state.showPermissionDeniedMessage, `is`(true))
    }

    @Test
    fun `activatePermissionStatusCheck - sets check flag to true`() = runTest {
        // Given initial state
        val initialState = viewModel.uiState.first()
        assertThat(initialState.isPermissionCheckInProgress, `is`(false))

        // When activating permission check
        viewModel.activatePermissionStatusCheck()
        advanceUntilIdle()

        // Then flag is set to true
        val state = viewModel.uiState.first()
        assertThat(state.isPermissionCheckInProgress, `is`(true))
    }

    @Test
    fun `resetActivityPermissionDenied - resets permission flags`() = runTest {
        // Given permission denied state
        viewModel.handlePermissionResult(false)
        advanceUntilIdle()
        val deniedState = viewModel.uiState.first()
        assertThat(deniedState.isActivityPermissionDenied, `is`(true))
        assertThat(deniedState.showPermissionDeniedMessage, `is`(true))

        // When resetting
        viewModel.resetActivityPermissionDenied()
        advanceUntilIdle()

        // Then flags are reset
        val state = viewModel.uiState.first()
        assertThat(state.isActivityPermissionDenied, `is`(false))
        assertThat(state.showPermissionDeniedMessage, `is`(false))
    }

    @Test
    fun `activityPermissionCheckComplete - sets check flag to false`() = runTest {
        // Given check in progress
        viewModel.activatePermissionStatusCheck()
        advanceUntilIdle()
        val checkingState = viewModel.uiState.first()
        assertThat(checkingState.isPermissionCheckInProgress, `is`(true))

        // When marking check complete
        viewModel.activityPermissionCheckComplete()
        advanceUntilIdle()

        // Then flag is reset
        val state = viewModel.uiState.first()
        assertThat(state.isPermissionCheckInProgress, `is`(false))
    }

    @Test
    fun `readyToNavigateToDailyProgress - sets navigation flag`() = runTest {
        // Given initial state
        val initialState = viewModel.uiState.first()
        assertThat(initialState.navigateToDailyProgress, `is`(false))

        // When setting ready to navigate
        viewModel.readyToNavigateToDailyProgress()
        advanceUntilIdle()

        // Then flag is set
        val state = viewModel.uiState.first()
        assertThat(state.navigateToDailyProgress, `is`(true))
    }

    @Test
    fun `navigationToDailyProgressComplete - resets navigation flag`() = runTest {
        // Given navigation flag is set
        viewModel.readyToNavigateToDailyProgress()
        advanceUntilIdle()
        val navigatingState = viewModel.uiState.first()
        assertThat(navigatingState.navigateToDailyProgress, `is`(true))

        // When navigation completes
        viewModel.navigationToDailyProgressComplete()
        advanceUntilIdle()

        // Then flag is reset
        val state = viewModel.uiState.first()
        assertThat(state.navigateToDailyProgress, `is`(false))
    }

    @Test
    fun `degradeHomeDestinationMenu - sets degradation flags`() = runTest {
        // Given initial state
        val initialState = viewModel.uiState.first()

        // When degrading home menu
        viewModel.degradeHomeDestinationMenu()
        advanceUntilIdle()

        // Then flags are set
        val state = viewModel.uiState.first()
        assertThat(state.isHomeScreenDegraded, `is`(true))
        assertThat(state.degradeHomeFunctionality, `is`(true))
    }

    @Test
    fun `upgradeHomeDestinationMenu - sets upgrade flags`() = runTest {
        // Given initial state
        val initialState = viewModel.uiState.first()

        // When upgrading home menu
        viewModel.upgradeHomeDestinationMenu()
        advanceUntilIdle()

        // Then flags are set
        val state = viewModel.uiState.first()
        assertThat(state.isHomeScreenDegraded, `is`(false))
        assertThat(state.upgradeHomeFunctionality, `is`(true))
    }

    @Test
    fun `resetUpgradedHomeDestinationMap - resets upgrade flag`() = runTest {
        // Given upgrade flag is set
        viewModel.upgradeHomeDestinationMenu()
        advanceUntilIdle()
        val upgradedState = viewModel.uiState.first()
        assertThat(upgradedState.upgradeHomeFunctionality, `is`(true))

        // When resetting upgrade
        viewModel.resetUpgradedHomeDestinationMap()
        advanceUntilIdle()

        // Then flag is reset
        val state = viewModel.uiState.first()
        assertThat(state.upgradeHomeFunctionality, `is`(false))
    }

    @Test
    fun `resetHomeDestinationMap - resets degradation flag`() = runTest {
        // Given degradation flag is set
        viewModel.degradeHomeDestinationMenu()
        advanceUntilIdle()
        val degradedState = viewModel.uiState.first()
        assertThat(degradedState.degradeHomeFunctionality, `is`(true))

        // When resetting degradation
        viewModel.resetHomeDestinationMap()
        advanceUntilIdle()

        // Then flag is reset
        val state = viewModel.uiState.first()
        assertThat(state.degradeHomeFunctionality, `is`(false))
    }

    @Test
    fun `setBottomNavVisibility - updates visibility flag`() = runTest {
        // Given collectors to track state changes
        val results = mutableListOf<Boolean>()
        val job = launch(testDispatcher) {
            viewModel.uiState.collect { results.add(it.bottomNavVisible) }
        }
        advanceUntilIdle() // Collect initial value (true)

        // When setting to false then true (to ensure distinct values)
        viewModel.setBottomNavVisibility(false)
        advanceUntilIdle()
        viewModel.setBottomNavVisibility(true)
        advanceUntilIdle()

        // Then visibility changes accordingly
        assertThat(results.size, `is`(3))
        assertThat(results[0], `is`(true)) // Default value
        assertThat(results[1], `is`(false))
        assertThat(results[2], `is`(true))

        job.cancel()
    }

    @Test
    fun `uiState - initially has correct default values`() = runTest {
        // Given a new ViewModel

        // When getting the initial UI state
        val initialState = viewModel.uiState.first()

        // Then default values are set correctly
        assertThat(initialState.navigateToDailyProgress, `is`(false))
        assertThat(initialState.upgradeHomeFunctionality, `is`(false))
        assertThat(initialState.degradeHomeFunctionality, `is`(false))
        assertThat(initialState.isActivityPermissionDenied, `is`(false))
        assertThat(initialState.isHomeScreenDegraded, `is`(false))
        assertThat(initialState.showPermissionDeniedMessage, `is`(false))
        assertThat(initialState.isPermissionCheckInProgress, `is`(false))
        assertThat(initialState.bottomNavVisible, `is`(true))
    }

    @Test
    fun `permission flow - when denied then reset then granted - transitions through all states correctly`() = runTest {
        // Given a collector to track state changes
        val permissionStates = mutableListOf<Boolean>() // tracks isActivityPermissionDenied
        val navigationStates = mutableListOf<Boolean>() // tracks navigateToDailyProgress

        val job = launch(testDispatcher) {
            viewModel.uiState.collect {
                permissionStates.add(it.isActivityPermissionDenied)
                navigationStates.add(it.navigateToDailyProgress)
            }
        }
        advanceUntilIdle() // Collect initial state

        // When permission is denied
        viewModel.handlePermissionResult(false)
        advanceUntilIdle()

        // Then denied state is set correctly
        assertThat(permissionStates.last(), `is`(true))
        assertThat(navigationStates.last(), `is`(false))

        // When permission state is reset
        viewModel.resetActivityPermissionDenied()
        advanceUntilIdle()

        // Then permission flags are reset
        assertThat(permissionStates.last(), `is`(false))
        assertThat(navigationStates.last(), `is`(false))

        // When permission is granted
        viewModel.handlePermissionResult(true)
        advanceUntilIdle()

        // Then granted state is set correctly
        assertThat(permissionStates.last(), `is`(false))
        assertThat(navigationStates.last(), `is`(true))

        job.cancel()
    }
}