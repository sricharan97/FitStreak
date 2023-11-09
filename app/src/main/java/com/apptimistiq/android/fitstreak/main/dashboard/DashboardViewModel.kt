package com.apptimistiq.android.fitstreak.main.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.UserInfoPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOG_TAG = "ProgressViewModel"

enum class GoalUserInfo {
    HEIGHT, WEIGHT, STEPS, WATER, EXERCISE, SLEEP, DEFAULT
}


class DashboardViewModel @Inject constructor(
    private val dataSource: ActivityDataSource
) : ViewModel() {

    private val _navigateToEditGoal = MutableStateFlow(GoalUserInfo.DEFAULT)

    val navigateToEditGoal: StateFlow<GoalUserInfo> = _navigateToEditGoal

    private val _navigateBackToDashboard = MutableStateFlow(false)

    val navigateBackToDashboard: StateFlow<Boolean> = _navigateBackToDashboard

    private val currentIncrDcrVal = MutableStateFlow(0)

    private val _displayedGoalValue = MutableStateFlow(0)

    val displayedGoalValue: StateFlow<Int> = _displayedGoalValue


    val goals: StateFlow<GoalPreferences?> = dataSource.getCurrentGoals().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalPreferences()
    )

    val userInfo: StateFlow<UserInfoPreferences> = dataSource.getCurrentUserInfo().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserInfoPreferences(168, 60)
    )

    private val _currentEditInfoType = MutableStateFlow(GoalUserInfo.DEFAULT)

    val goalInfoVal: StateFlow<Int> = _currentEditInfoType.flatMapLatest { goalUserInfoType ->
        dataSource.getCurrentGoalUserInfo(goalUserInfoType)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun saveGoalInfo() {
        viewModelScope.launch {
            dataSource.saveGoalInfo(_currentEditInfoType.value, _displayedGoalValue.value)
        }

        navigateBackDashboardFragment()

    }

    fun navigateEditGoal(goal_info_type: GoalUserInfo) {
        _navigateToEditGoal.update {
            goal_info_type
        }
        updateCurrentEditInfoType(goal_info_type)
    }

    fun navigateToEditGoalCompleted() {
        _navigateToEditGoal.update { GoalUserInfo.DEFAULT }
    }

    private fun updateCurrentEditInfoType(goal_info_type: GoalUserInfo) {

        _currentEditInfoType.update {
            goal_info_type
        }
        when (goal_info_type) {
            GoalUserInfo.SLEEP, GoalUserInfo.HEIGHT, GoalUserInfo.WATER,
            GoalUserInfo.WEIGHT -> {
                currentIncrDcrVal.update { 1 }
            }

            GoalUserInfo.STEPS, GoalUserInfo.EXERCISE -> {
                currentIncrDcrVal.update { 500 }
            }
            else -> {
                currentIncrDcrVal.update { 0 }
            }

        }
    }

    fun updateDisplayedGoalInfoVal(value: Int) {
        _displayedGoalValue.update {
            value
        }

    }


    fun incrementGoalInfoValue() {
        _displayedGoalValue.update {
            it + currentIncrDcrVal.value
        }
    }

    fun decrementGoalInfoValue() {

        _displayedGoalValue.update {
            it - currentIncrDcrVal.value
        }
    }

    private fun navigateBackDashboardFragment() {
        _navigateBackToDashboard.update { true }
    }

    fun navigateBackDashboardFragmentComplete() {
        _navigateBackToDashboard.update { false }
    }


}