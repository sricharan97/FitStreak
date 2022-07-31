package com.apptimistiq.android.fitstreak.authentication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.GoalDataSource
import kotlinx.coroutines.launch

enum class GoalType {
    STEP,
    WATER,
    EXERCISE,
    SLEEP

}

class AuthenticationViewModel(
    val app: Application,
    private val dataSource: GoalDataSource
) : AndroidViewModel(app) {


    fun saveGoal(goalType: GoalType, value: Int) {
        when (goalType) {
            GoalType.STEP -> viewModelScope.launch { dataSource.updateStepGoal(value) }
            GoalType.EXERCISE -> viewModelScope.launch { dataSource.updateExerciseGoal(value) }
            GoalType.SLEEP -> viewModelScope.launch { dataSource.updateSleepGoal(value) }
            GoalType.WATER -> viewModelScope.launch { dataSource.updateWaterGlassesGoal(value) }
        }
    }

}