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


}