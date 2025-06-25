package com.apptimistiq.android.fitstreak.main.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptimistiq.android.fitstreak.main.data.ActivityDataSource
import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.GoalUserInfo
import com.apptimistiq.android.fitstreak.main.data.domain.UserInfoPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val LOG_TAG = "DashboardViewModel"

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

    //region Data Streams

    /**
     * StateFlow that emits the current user's fitness goals.
     * It is backed by a data source and shared across the ViewModel's scope.
     */
    val goals: StateFlow<GoalPreferences?> = dataSource.getCurrentGoals().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val userInitialsState: StateFlow<String> = dataSource.getCurrentUserState()
        .map { getInitialsFromName(it.userName) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "NA"
        )




    // Weekly activities data
    private val _weeklyActivities = MutableStateFlow<List<Activity>>(emptyList())
    val weeklyActivities: StateFlow<List<Activity>> = _weeklyActivities

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    //endregion

    //region Navigation and Editing State

    private val _navigateToEditGoal = MutableStateFlow(GoalUserInfo.DEFAULT)
    val navigateToEditGoal: StateFlow<GoalUserInfo> = _navigateToEditGoal

    private val _navigateBackToDashboard = MutableStateFlow(false)
    val navigateBackToDashboard: StateFlow<Boolean> = _navigateBackToDashboard

    private val _currentEditInfoType = MutableStateFlow(GoalUserInfo.DEFAULT)
    /**
     * StateFlow that provides the current value for the goal or user info being edited.
     * It dynamically switches its data source based on the type of information being edited.
     */
    val goalInfoVal: StateFlow<Int> = _currentEditInfoType.flatMapLatest { goalUserInfoType ->
        dataSource.getCurrentGoalUserInfo(goalUserInfoType)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )

    private val _displayedGoalValue = MutableStateFlow(0)
    val displayedGoalValue: StateFlow<Int> = _displayedGoalValue

    //endregion

    init {
        fetchWeeklyActivities()
    }

    private fun fetchWeeklyActivities() {
        viewModelScope.launch {
            _isLoading.value = true
            dataSource.getWeekActivities()
                .catch { e ->
                    _error.value = "Failed to load weekly activities: ${e.message}"
                    _isLoading.value = false
                }
                .collect { activities ->
                    _weeklyActivities.value = activities
                    _isLoading.value = false
                }
        }
    }


    //region Navigation and Editing Functions

    /**
     * Initiates navigation to the goal editing screen for a specific goal type.
     *
     * @param goal_info_type The type of goal or user info to be edited
     */
    fun navigateEditGoal(goal_info_type: GoalUserInfo) {
        updateCurrentEditInfoType(goal_info_type)
        _navigateToEditGoal.update { goal_info_type }
    }

    /**
     * Updates the internal state to reflect the current type of goal being edited.
     *
     * @param goal_info_type The type of goal or user info being edited
     */
    private fun updateCurrentEditInfoType(goal_info_type: GoalUserInfo) {
        _currentEditInfoType.update { goal_info_type }
    }

    /**
     * Resets the navigation trigger after navigation to the edit screen has occurred.
     */
    fun navigateToEditGoalCompleted() {
        _navigateToEditGoal.update { GoalUserInfo.DEFAULT }
    }

    /**
     * Triggers navigation back to the dashboard screen.
     */
    private fun navigateBackDashboardFragment() {
        _navigateBackToDashboard.update { true }
    }

    /**
     * Updates the displayed value for the goal being edited.
     *
     * @param value The new value to be displayed
     */
    fun updateDisplayedGoalInfoVal(value: Int) {
        _displayedGoalValue.update { value }
    }

    private fun getIncrementValue(): Int {
        return when (_currentEditInfoType.value) {
            GoalUserInfo.STEPS -> 500
            GoalUserInfo.EXERCISE -> 50
            else -> 1
        }
    }

    /**
     * Increments the value of the goal being edited based on its type.
     */
    fun incrementGoalInfoValue() {
        _displayedGoalValue.update { it + getIncrementValue() }
    }

    /**
     * Decrements the value of the goal being edited, ensuring it doesn't go below zero.
     */
    fun decrementGoalInfoValue() {
        _displayedGoalValue.update { (it - getIncrementValue()).coerceAtLeast(0) }
    }

    /**
     * Resets the navigation trigger after returning to the dashboard.
     */
    fun navigateBackDashboardFragmentComplete() {
        _navigateBackToDashboard.update { false }
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

    //region Utility Functions

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _error.value = null
    }

    private fun generateChartData(valueExtractor: (Activity) -> Number): List<Pair<String, Float>> {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        // Create a map of activities by date for quick lookup.
        // The key will be the date formatted as "yyyy-MM-dd".
        val activitiesByDate = weeklyActivities.value.associateBy {
            // Convert seconds to milliseconds for Date object
            val date = Date(it.dateOfActivity * 1000)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }

        val chartData = mutableListOf<Pair<String, Float>>()

        // Iterate through the last 7 days, starting from 6 days ago up to today.
        for (i in 6 downTo 0) {
            val dayCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dayDate = dayCalendar.time
            val dayLabel = dayFormat.format(dayDate)
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayDate)

            val activity = activitiesByDate[dateKey]
            val value = activity?.let { valueExtractor(it).toFloat() } ?: 0f

            chartData.add(Pair(dayLabel, value))
        }
        return chartData
    }


    /**
     * Get data for steps chart
     */
    fun getStepsData(): List<Pair<String, Float>> {
        val data = generateChartData { it.steps }
        return data
    }

    /**
     * Get data for water chart
     */
    fun getWaterData(): List<Pair<String, Float>> {
        val data = generateChartData { it.waterGlasses }
        return data
    }

    /**
     * Get data for exercise chart
     */
    fun getExerciseData(): List<Pair<String, Float>> {
        val data = generateChartData { it.exerciseCalories }
        return data
    }

    /**
     * Get data for sleep chart
     */
    fun getSleepData(): List<Pair<String, Float>> {
        val data = generateChartData { it.sleepHours }
        return data
    }


    /**
     * Generates initials from the user's full name.
     * Returns up to 2 characters representing the user's initials.
     *
     * @param fullName The user's full name
     * @return String containing the user's initials
     */
    fun getInitialsFromName(fullName: String): String {
        if (fullName.isBlank()) return "NA"

        return fullName.split(" ")
            .filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().uppercase() }
    }
    //endregion
}