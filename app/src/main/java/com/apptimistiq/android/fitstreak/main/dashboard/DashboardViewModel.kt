package com.apptimistiq.android.fitstreak.main.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import com.apptimistiq.android.fitstreak.main.data.domain.UserInfoPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOG_TAG = "ProgressViewModel"

/**
 * ViewModel for the Dashboard screen that handles user goals and information.
 * 
 * This ViewModel is responsible for:
 * - Retrieving and displaying current user goals and info
 * - Handling goal editing interactions
 * - Managing navigation between dashboard and edit screens
 * - Persisting updated goal values
 */
class DashboardViewModel @Inject constructor(
    private val dataSource: ActivityDataSource
) : ViewModel() {

    //region Navigation State
    
    private val _navigateToEditGoal = MutableStateFlow(GoalUserInfo.DEFAULT)
    val navigateToEditGoal: StateFlow<GoalUserInfo> = _navigateToEditGoal

    private val _navigateBackToDashboard = MutableStateFlow(false)
    val navigateBackToDashboard: StateFlow<Boolean> = _navigateBackToDashboard
    
    /**
     * Triggers navigation to goal editing screen for the specified goal type.
     * 
     * @param goal_info_type The type of goal or user info to edit
     */
    fun navigateEditGoal(goal_info_type: GoalUserInfo) {
        _navigateToEditGoal.update {
            goal_info_type
        }
        updateCurrentEditInfoType(goal_info_type)
    }

    /**
     * Resets navigation state after navigation to edit screen is complete.
     */
    fun navigateToEditGoalCompleted() {
        _navigateToEditGoal.update { GoalUserInfo.DEFAULT }
    }

    /**
     * Triggers navigation back to dashboard after changes are saved.
     */
    private fun navigateBackDashboardFragment() {
        _navigateBackToDashboard.update { true }
    }

    /**
     * Resets navigation state after returning to dashboard is complete.
     */
    fun navigateBackDashboardFragmentComplete() {
        _navigateBackToDashboard.update { false }
    }
    //endregion

    //region Data Streams
    
    /**
     * Flow of current user goal preferences.
     */
    val goals: StateFlow<GoalPreferences?> = dataSource.getCurrentGoals().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalPreferences()
    )

    /**
     * Flow of current user information preferences.
     */
    val userInfo: StateFlow<UserInfoPreferences> = dataSource.getCurrentUserInfo().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserInfoPreferences(168, 60)
    )
    //endregion

    //region Goal Editing State
    
    private val _currentEditInfoType = MutableStateFlow(GoalUserInfo.DEFAULT)
    private val currentIncrDcrVal = MutableStateFlow(0)
    
    private val _displayedGoalValue = MutableStateFlow(0)
    val displayedGoalValue: StateFlow<Int> = _displayedGoalValue

    /**
     * Flow of the current value for the goal type being edited.
     * Changes based on the selected goal type.
     */
    val goalInfoVal: StateFlow<Int> = _currentEditInfoType.flatMapLatest { goalUserInfoType ->
        dataSource.getCurrentGoalUserInfo(goalUserInfoType)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    /**
     * Updates the currently selected goal/info type and sets the appropriate increment/decrement step value.
     *
     * @param goal_info_type The type of goal or user info being edited
     */
    private fun updateCurrentEditInfoType(goal_info_type: GoalUserInfo) {
        _currentEditInfoType.update {
            goal_info_type
        }
        // Set the increment/decrement step based on the type of goal
        when (goal_info_type) {
            // Smaller increments for height, weight, water, and sleep metrics
            GoalUserInfo.SLEEP, GoalUserInfo.HEIGHT, GoalUserInfo.WATER,
            GoalUserInfo.WEIGHT -> {
                currentIncrDcrVal.update { 1 }
            }
            // Larger increments for steps and exercise duration
            GoalUserInfo.STEPS, GoalUserInfo.EXERCISE -> {
                currentIncrDcrVal.update { 500 }
            }
            else -> {
                currentIncrDcrVal.update { 0 }
            }
        }
    }

    /**
     * Updates the displayed goal value being edited.
     *
     * @param value The new value to display
     */
    fun updateDisplayedGoalInfoVal(value: Int) {
        _displayedGoalValue.update {
            value
        }
    }

    /**
     * Increases the displayed goal value by the current increment step.
     */
    fun incrementGoalInfoValue() {
        _displayedGoalValue.update {
            it + currentIncrDcrVal.value
        }
    }

    /**
     * Decreases the displayed goal value by the current decrement step.
     */
    fun decrementGoalInfoValue() {
        _displayedGoalValue.update {
            it - currentIncrDcrVal.value
        }
    }
    //endregion
    
    //region Data Persistence

    /**
     * Persists the edited goal value to the data source and navigates back to the dashboard.
     */
    fun saveGoalInfo() {
        viewModelScope.launch {
            dataSource.saveGoalInfo(_currentEditInfoType.value, _displayedGoalValue.value)
        }
        navigateBackDashboardFragment()
    }
    //endregion
}
