package com.apptimistiq.android.fitstreak.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
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
    private val dataSource: ActivityDataSource
) : ViewModel() {


    fun saveGoal(goalType: GoalType, value: Int) {
        viewModelScope.launch {
            dataSource.saveGoal(goalType, value)
        }

    }

}