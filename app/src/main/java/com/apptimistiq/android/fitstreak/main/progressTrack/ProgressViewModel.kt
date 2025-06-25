package com.apptimistiq.android.fitstreak.main.progressTrack

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import com.apptimistiq.android.fitstreak.main.data.domain.ProgressTrackUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val LOG_TAG = "ProgressViewModel"

/**
 * ViewModel responsible for managing user activity data and progress tracking.
 * Handles communication between UI and data layers, manages activity state,
 * and facilitates integration with Google Fit.
 *
 * @property dataSource The data source that provides access to activity repositories
 */
class ProgressViewModel @Inject constructor(
    private val dataSource: ActivityDataSource
) : ViewModel() {

    // region Constants and helper properties
    
    /** Current formatted time for workout tracking */
    private val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    
    /** Current date (start of day) in seconds since epoch */
    private val currentDate = DateTime().withTimeAtStartOfDay().millis.div(1000)
    
    // endregion

    // region UI State management
    
    /**
     * Main UI state holder for the progress tracking screen
     */
    private val _uiState = MutableStateFlow(ProgressTrackUiState())
    val uiState: StateFlow<ProgressTrackUiState> = _uiState

    /**
     * Navigation trigger for activity edit screen
     */
    private val _navigateEditActivity = MutableStateFlow(ActivityType.DEFAULT)
    val navigateEditActivity: StateFlow<ActivityType> = _navigateEditActivity

    /**
     * Navigation trigger for returning to the progress screen
     */
    private val _navigateBackProgress = MutableStateFlow(false)
    val navigateBackProgress: StateFlow<Boolean> = _navigateBackProgress
    
    // endregion

    // region Activity tracking
    
    /**
     * Currently selected activity type
     */
    private val _currentActivityType = MutableStateFlow(ActivityType.DEFAULT)
    val currentActivityType: StateFlow<ActivityType> = _currentActivityType

    /**
     * Currently displayed activity value (used for editing)
     */
    val _displayedActivityValue = MutableStateFlow(0)
    val displayedActivityValue: StateFlow<Int> = _displayedActivityValue

    /**
     * Collects current value for selected activity from data source
     */
    val activityValueCurrent: StateFlow<Int> = _currentActivityType.flatMapLatest {
        dataSource.getCurrentActivityVal(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    /**
     * Today's activity items from data source
     */
    val activityItemsToday: StateFlow<List<ActivityItemUiState>?> =
        dataSource.getTodayActivity().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Temporary storage for activity items before saving or updating
     */
    private val activityItemUiStateList: ArrayList<ActivityItemUiState> = ArrayList()
    
    // endregion

    // region Google Fit integration
    
    /**
     * Tracks water intake updates for Google Fit
     */
    private val _updateFitWater = MutableStateFlow(0)
    val updateFitWater: StateFlow<Int> = _updateFitWater

    /**
     * Tracks sleep duration updates for Google Fit
     */
    private val _updateFitSleep = MutableStateFlow(0)
    val updateFitSleep: StateFlow<Int> = _updateFitSleep

    /**
     * Tracks exercise duration updates for Google Fit
     */
    private val _updateFitExercise = MutableStateFlow(0)
    val updateFitExercise = _updateFitExercise

    /**
     * Exercise start time for Google Fit
     */
    val updateFitExerciseStartTime = MutableStateFlow(currentTime)
    val updateFitExerciseStartTimeObs = updateFitExerciseStartTime

    /**
     * Exercise end time for Google Fit
     */
    val updateFitExerciseEndTime = MutableStateFlow(currentTime)
    val updateFitExerciseEndTimeObs = updateFitExerciseEndTime
    
    // endregion

    // region Permission and access handling
    
    /**
     * Signals that Google Fit access is granted
     */
    fun accessGoogleFit() {
        _uiState.update { currentUiState ->
            currentUiState.copy(canAccessGoogleFit = true)
        }
    }

    /**
     * Signals that subscription process is completed
     */
    fun doneWithSubscription() {
        _uiState.update { currentUiState ->
            currentUiState.copy(subscriptionDone = true)
        }
    }
    
    // endregion

    // region Activity data addition methods
    
    /**
     * Adds step count to the activity list
     * 
     * @param reading The step count value to add
     */
    fun addSteps(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.STEP, reading))
        _uiState.update { currentUiState ->
            currentUiState.copy(readSteps = true)
        }
    }

    /**
     * Adds calorie count to the activity list
     * 
     * @param reading The calorie count value to add
     */
    fun addCalories(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.EXERCISE, reading))
        _uiState.update { currentUiState ->
            currentUiState.copy(readCalories = true)
        }
    }

    /**
     * Adds water intake to the activity list
     * 
     * @param reading The water intake value to add (in litres)
     */
    fun addLitres(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.WATER, reading))
        _uiState.update { currentUiState ->
            currentUiState.copy(readWaterLitres = true)
        }
    }

    /**
     * Adds sleep duration to the activity list
     * 
     * @param reading The sleep duration value to add (in hours)
     */
    fun addSleepHrs(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.SLEEP, reading))
        _uiState.update { currentUiState ->
            currentUiState.copy(readSleepHrs = true)
        }
    }
    
    // endregion

    // region Activity data persistence

    /**
     * Saves activity data - determines whether to create a new record
     * or update an existing one based on today's data
     */
    fun saveActivity() {
        activityItemsToday.value?.let {
            if (it.isNotEmpty()) {
                updateActivity()
                return
            }
        }

        viewModelScope.launch {

            dataSource.saveActivity(activityItemUiStateList, currentDate)
        }
        activitySaved()
    }

    /**
     * Updates an existing activity record for today
     */
    private fun updateActivity() {

        viewModelScope.launch {
            dataSource.updateActivity(activityItemUiStateList, currentDate)
        }
        activitySaved()
    }

    /**
     * Updates UI state to reflect that activity has been saved
     */
    private fun activitySaved() {
        _uiState.update {
            it.copy(activitySavedForDay = true)
        }
    }
    
    // endregion

    // region Navigation control

    /**
     * Initiates navigation to edit activity screen
     * 
     * @param activityType The type of activity to edit
     */
    fun navigateToEditActivity(activityType: ActivityType) {
        _navigateEditActivity.update {
            activityType
        }
        updateCurrentActivityType(activityType)
    }

    /**
     * Resets navigation state after navigation is complete
     */
    fun navigateToEditActivityCompleted() {
        _navigateEditActivity.update {
            ActivityType.DEFAULT
        }
    }

    /**
     * Initiates navigation back to progress tracking screen
     */
    private fun navigateBackToProgressFragment() {
        _navigateBackProgress.update { true }
    }

    /**
     * Resets navigation state after back navigation is complete
     */
    fun navigateBackToProgressFragmentCompleted() {
        _navigateBackProgress.update {
            false
        }
    }
    
    // endregion

    // region Activity editing functions

    /**
     * Updates the currently selected activity type
     * 
     * @param activityType The new activity type to select
     */
    private fun updateCurrentActivityType(activityType: ActivityType) {
        _currentActivityType.update {
            activityType
        }
    }

    /**
     * Updates the displayed activity value
     * 
     * @param value The new value to display
     */
    fun updateDisplayedActivityVal(value: Int) {
        _displayedActivityValue.update {
            value
        }
    }

    /**
     * Increments the displayed activity value by 1
     */
    fun incrementActivityValue() {
        _displayedActivityValue.update {
            it + 1
        }
    }

    /**
     * Decrements the displayed activity value by 1
     */
    fun decrementActivityValue() {
        _displayedActivityValue.update {
            it - 1
        }
    }

    /**
     * Updates the activity value in the Google Fit store based on user input
     * and navigates back to the progress screen
     */
    fun updateUserActivityVal() {
        activityItemsToday.value?.forEach { activityItem ->


            if (_currentActivityType.value == activityItem.dataType) {
                when (_currentActivityType.value) {
                    ActivityType.SLEEP -> {
                        _updateFitSleep.update { _displayedActivityValue.value }

                    }

                    ActivityType.EXERCISE -> {
                        val updatedVal = _displayedActivityValue.value
                        _updateFitExercise.update { updatedVal }
                    }

                    ActivityType.WATER -> {
                        val updatedVal =
                            _displayedActivityValue.value
                        _updateFitWater.update { updatedVal }
                    }
                    else -> {}
                }
            }
        }

        navigateBackToProgressFragment()
    }

    /**
     * Updates activity values in the database after the data is updated in Google Fit
     * 
     * @param updatedValue The new value to save
     */
    /**
     * Updates activity values in the database after the data is updated in Google Fit
     *
     * @param updatedValue The new value to save
     */
    fun updateUserEnteredValues(updatedValue: Int) {
        val editActivityItemList = ArrayList<ActivityItemUiState>()

        activityItemsToday.value?.forEach { activityItem ->
            if (_currentActivityType.value == activityItem.dataType) {
                editActivityItemList.add(activityItem.copy(currentReading = updatedValue))
            } else {
                editActivityItemList.add(activityItem)
            }
        }

        viewModelScope.launch {
            dataSource.updateActivity(editActivityItemList, currentDate)
        }
    }
    
    // endregion

    // region Google Fit update completion handlers
    
    /**
     * Resets water update tracking after Google Fit sync
     */
    fun fitWaterUpdated() {
        _updateFitWater.update {
            0
        }
    }

    /**
     * Resets exercise update tracking after Google Fit sync
     */
    fun fitExerciseUpdated() {
        _updateFitExercise.update {
            0
        }
    }

    /**
     * Resets sleep update tracking after Google Fit sync
     */
    fun fitSleepUpdated() {
        _updateFitSleep.update {
            0
        }
    }
    
    // endregion
}
