package com.apptimistiq.android.fitstreak.main.progressTrack

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


// @Inject tells Dagger how to provide instances of this type
class ProgressViewModel @Inject constructor(
    private val dataSource: ActivityDataSource
) : ViewModel() {


    private val _uiState = MutableStateFlow(ProgressTrackUiState())
    val uiState: StateFlow<ProgressTrackUiState> = _uiState

    val activityItemsToday: StateFlow<List<ActivityItemUiState>> =
        dataSource.getTodayActivity().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()

        )


    //Store a reference to a list of ActivityItemUiState objects
    private val activityItemUiStateList: ArrayList<ActivityItemUiState> = ArrayList()


    //Get today's date to be used in the functions
    private val currentDate = DateTime().withTimeAtStartOfDay().millis


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
    }

    fun addCalories(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.EXERCISE, reading))
    }

    fun addLitres(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.WATER, reading))
    }

    fun addSleepHrs(reading: Int) {
        activityItemUiStateList.add(ActivityItemUiState(ActivityType.SLEEP, reading))
    }


    //insert a new activity record in case this is the first time that app opens.
    fun saveActivity() {

        if (activityItemsToday.value.isEmpty()) {
            viewModelScope.launch {
                dataSource.saveActivity(activityItemUiStateList, currentDate)
            }
        } else {
            updateActivity()
        }


    }


    // Update the step count for the existing activity record
    private fun updateActivity() {

        viewModelScope.launch {
            dataSource.updateActivity(activityItemUiStateList, currentDate)
        }

    }


}