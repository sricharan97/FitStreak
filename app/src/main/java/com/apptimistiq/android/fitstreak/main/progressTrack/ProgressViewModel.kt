package com.apptimistiq.android.fitstreak.main.progressTrack

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.ProgressTrackUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class ProgressViewModel(
    val app: Application,
    private val dataSource: ActivityDataSource
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(ProgressTrackUiState(isFetchingActivities = true))
    val uiState: StateFlow<ProgressTrackUiState> = _uiState


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

    fun updateSteps(totalSteps: Int) {

        val currentDate = DateTime().withTimeAtStartOfDay().millis

        viewModelScope.launch {
            dataSource.updateActivity(
                Activity(
                    id = 0,
                    steps = totalSteps,
                    dateOfActivity = currentDate
                )
            )
        }

    }


}