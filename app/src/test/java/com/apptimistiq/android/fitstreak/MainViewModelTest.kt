package com.apptimistiq.android.fitstreak

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.apptimistiq.android.fitstreak.main.MainViewModel
import com.apptimistiq.android.fitstreak.main.home.PermissionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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
    fun `onPermissionResult with grant, updates state and emits navigation event`() = runTest {
        // ARRANGE
        var event: PermissionEvent? = null
        val job = launch {
            viewModel.permissionEvent.collect { event = it }
        }

        // ACT
        viewModel.onPermissionResult(isGranted = true, shouldShowRationale = false)
        advanceUntilIdle()

        // ASSERT
        assertTrue(viewModel.uiState.value.isActivityPermissionGranted)
        assertEquals(PermissionEvent.NavigateToDailyProgress, event)
        job.cancel()
    }

    @Test
    fun `onPermissionResult with denial and rationale, emits rationale event`() = runTest {
        // ARRANGE
        var event: PermissionEvent? = null
        val job = launch {
            viewModel.permissionEvent.collect { event = it }
        }

        // ACT
        viewModel.onPermissionResult(isGranted = false, shouldShowRationale = true)
        advanceUntilIdle()

        // ASSERT
        assertFalse(viewModel.uiState.value.isActivityPermissionGranted)
        assertEquals(PermissionEvent.ShowPermissionRationale, event)
        job.cancel()
    }

    @Test
    fun `onPermissionResult with denial and no rationale, emits no event`() = runTest {
        // ARRANGE
        var event: PermissionEvent? = null
        val job = launch {
            viewModel.permissionEvent.collect { event = it }
        }

        // ACT
        viewModel.onPermissionResult(isGranted = false, shouldShowRationale = false)
        advanceUntilIdle()

        // ASSERT
        assertFalse(viewModel.uiState.value.isActivityPermissionGranted)
        assertEquals(null, event)
        job.cancel()
    }

    @Test
    fun `setBottomNavVisibility, updates uiState correctly`() = runTest {
        // ACT & ASSERT
        viewModel.setBottomNavVisibility(false)
        assertFalse(viewModel.uiState.value.bottomNavVisible)

        viewModel.setBottomNavVisibility(true)
        assertTrue(viewModel.uiState.value.bottomNavVisible)
    }

    @Test
    fun `initial uiState is correct`() = runTest {
        // ACT
        val initialState = viewModel.uiState.first()

        // ASSERT
        assertEquals(false, initialState.bottomNavVisible)
        assertEquals(false, initialState.isActivityPermissionGranted)
    }
}
