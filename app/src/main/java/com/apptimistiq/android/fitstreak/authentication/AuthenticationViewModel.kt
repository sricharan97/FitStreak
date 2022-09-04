package com.apptimistiq.android.fitstreak.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.GoalDataSource
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GoalType {
    STEP,
    WATER,
    EXERCISE,
    SLEEP

}

// @Inject tells Dagger how to provide instances of this type
class AuthenticationViewModel @Inject constructor(
    private val dataSource: GoalDataSource
) : ViewModel() {


    fun saveGoal(goalType: GoalType, value: Int) {
        when (goalType) {
            GoalType.STEP -> viewModelScope.launch { dataSource.updateStepGoal(value) }
            GoalType.EXERCISE -> viewModelScope.launch { dataSource.updateExerciseGoal(value) }
            GoalType.SLEEP -> viewModelScope.launch { dataSource.updateSleepGoal(value) }
            GoalType.WATER -> viewModelScope.launch { dataSource.updateWaterGlassesGoal(value) }
        }
    }

}