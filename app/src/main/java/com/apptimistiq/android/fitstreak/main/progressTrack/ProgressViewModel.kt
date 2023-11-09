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
import javax.inject.Inject


private const val LOG_TAG = "ProgressViewModel"

// @Inject tells Dagger how to provide instances of this type
class ProgressViewModel @Inject constructor(
    private val dataSource: ActivityDataSource
) : ViewModel() {


    //Stateflow variables that keep track of changes in updates to the activity values
    //to update the google fit repository values as well.

    private val _updateFitSteps = MutableStateFlow(0)
    val updateFitSteps: StateFlow<Int> = _updateFitSteps

    private val _updateFitWater = MutableStateFlow(0)
    val updateFitWater: StateFlow<Int> = _updateFitWater

    private val _updateFitSleep = MutableStateFlow(0)
    val updateFitSleep: StateFlow<Int> = _updateFitSleep

    private val _updateFitExercise = MutableStateFlow(0)
    val updateFitExercise = _updateFitExercise

    private val _navigateEditActivity = MutableStateFlow(ActivityType.DEFAULT)
    val navigateEditActivity: StateFlow<ActivityType> = _navigateEditActivity

    private val _navigateBackProgress = MutableStateFlow(false)
    val navigateBackProgress: StateFlow<Boolean> = _navigateBackProgress

    private val _currentActivityType = MutableStateFlow(ActivityType.DEFAULT)
    val currentActivityType: StateFlow<ActivityType> = _currentActivityType

    val activityValueCurrent: StateFlow<Int> = _currentActivityType.flatMapLatest {
        dataSource.getCurrentActivityVal(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    private val _displayedActivityValue = MutableStateFlow(0)

    val displayedActivityValue: StateFlow<Int> = _displayedActivityValue

    private val _uiState = MutableStateFlow(ProgressTrackUiState())
    val uiState: StateFlow<ProgressTrackUiState> = _uiState

    val activityItemsToday: StateFlow<List<ActivityItemUiState>?> =
        dataSource.getTodayActivity().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()

        )


    //Store a reference to a list of ActivityItemUiState objects
    private val activityItemUiStateList: ArrayList<ActivityItemUiState> = ArrayList()


    //Get today's date to be used in the functions
    private val currentDate = DateTime().withTimeAtStartOfDay().millis.div(1000)


    fun accessGoogleFit() {

        _uiState.update { currentUiState ->
            currentUiState.copy(canAccessGoogleFit = true)
        }
    }

    fun doneWithSubscription() {

        _uiState.update { currentUiState ->
            currentUiState.copy(subscriptionDone = true)
        }

    }


    fun addSteps(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.STEP, reading))
        Log.d(LOG_TAG, "Inside add steps method with the steps reading passed - $reading")
        _uiState.update { currentUiState ->
            currentUiState.copy(readSteps = true)
        }
    }

    fun addCalories(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.EXERCISE, reading))
        Log.d(LOG_TAG, "Inside add calories method with the calories reading passed - $reading")
        _uiState.update { currentUiState ->
            currentUiState.copy(readCalories = true)
        }
    }

    fun addLitres(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.WATER, reading))
        Log.d(LOG_TAG, "Inside add water method with the litres reading passed - $reading")
        _uiState.update { currentUiState ->
            currentUiState.copy(readWaterLitres = true)
        }
    }

    fun addSleepHrs(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.SLEEP, reading))
        Log.d(LOG_TAG, "Inside add sleep hrs method with the sleep hrs reading passed - $reading")
        _uiState.update { currentUiState ->
            currentUiState.copy(readSleepHrs = true)
        }
    }


    //insert a new activity record in case this is the first time that app opens.
    fun saveActivity() {

        activityItemsToday.value?.let {
            if (it.isNotEmpty()) {
                updateActivity()
                return
            }
        }

        viewModelScope.launch {
            Log.d(
                LOG_TAG, "Seems like a new entry and calling the saveActivity method with " +
                        "current activity list value = $activityItemUiStateList"
            )

            dataSource.saveActivity(activityItemUiStateList, currentDate)
        }
        activitySaved()

    }


    // Update the step count for the existing activity record
    private fun updateActivity() {

        Log.d(
            LOG_TAG, "Inside the updateActivity method with current activityList value" +
                    "= $activityItemUiStateList"
        )

        viewModelScope.launch {
            dataSource.updateActivity(activityItemUiStateList, currentDate)
        }
        activitySaved()

    }


    private fun activitySaved() {
        _uiState.update {
            it.copy(activitySavedForDay = true)
        }
    }


    fun navigateToEditActivity(activityType: ActivityType) {


        _navigateEditActivity.update {
            activityType
        }
        updateCurrentActivityType(activityType)


    }

    fun navigateToEditActivityCompleted() {
        _navigateEditActivity.update {
            ActivityType.DEFAULT
        }
    }

    private fun navigateBackToProgressFragment() {
        _navigateBackProgress.update { true }
    }

    fun navigateBackToProgressFragmentCompleted() {
        _navigateBackProgress.update {
            false
        }
    }

    private fun updateCurrentActivityType(activityType: ActivityType) {

        _currentActivityType.update {
            activityType
        }

    }

    fun updateDisplayedActivityVal(value: Int) {
        _displayedActivityValue.update {
            value
        }

    }


    fun incrementActivityValue() {
        _displayedActivityValue.update {
            it + 1
        }
    }

    fun decrementActivityValue() {

        _displayedActivityValue.update {
            it - 1
        }
    }

    fun updateUserActivityVal() {
        val editActivityItemList = ArrayList<ActivityItemUiState>()

        activityItemsToday.value?.forEach { activityItem ->
            Log.d(
                LOG_TAG,
                "Inside updateUSerActivityVal with current Activity item being ${activityItem.dataType}"
            )

            Log.d(
                LOG_TAG,
                "Inside updateUSerActivityVal with current Activity value ${_currentActivityType.value}"
            )

            if (_currentActivityType.value == activityItem.dataType) {
                when (_currentActivityType.value) {
                    ActivityType.STEP -> {
                        val updatedVal =
                            _displayedActivityValue.value - activityItem.currentReading
                        _updateFitSteps.update { updatedVal }
                        Log.d(LOG_TAG, "Steps value calculated after edit is $updatedVal")
                    }

                    ActivityType.SLEEP -> {

                        _updateFitSleep.update { _displayedActivityValue.value }
                        Log.d(
                            LOG_TAG,
                            "sleep value calculated after edit is ${_displayedActivityValue.value}"
                        )
                    }

                    ActivityType.EXERCISE -> {
                        val updatedVal =
                            _displayedActivityValue.value - activityItem.currentReading
                        _updateFitExercise.update { updatedVal }
                        Log.d(LOG_TAG, "exercise value calculated after edit is $updatedVal")
                    }

                    ActivityType.WATER -> {
                        val updatedVal =
                            _displayedActivityValue.value - activityItem.currentReading
                        _updateFitWater.update { updatedVal }
                        Log.d(LOG_TAG, "water value calculated after edit is $updatedVal")
                    }
                    else -> {}
                }


            }
            if (_currentActivityType.value == activityItem.dataType) {
                editActivityItemList.add(activityItem.copy(currentReading = _displayedActivityValue.value))
            } else {
                editActivityItemList.add(activityItem)
            }
        }

        viewModelScope.launch {
            dataSource.updateActivity(editActivityItemList, currentDate)
        }
        navigateBackToProgressFragment()
    }

    fun fitStepsUpdated() {
        _updateFitSteps.update {
            0
        }
    }

    fun fitWaterUpdated() {
        _updateFitWater.update {
            0
        }
    }

    fun fitExerciseUpdated() {
        _updateFitExercise.update {
            0
        }
    }

    fun fitSleepUpdated() {
        _updateFitSleep.update {
            0
        }
    }


}