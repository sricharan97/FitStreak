package com.apptimistiq.android.fitstreak.main.data.test

import com.apptimistiq.android.fitstreak.main.data.UserProfileDataSource
import com.apptimistiq.android.fitstreak.main.data.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeUserProfileDataSource : UserProfileDataSource {
    // Configurable test data
    private val _userStateInfo = MutableStateFlow(
        UserStateInfo(
            uid = "test-uid",
            userName = "Test User",
            isUserLoggedIn = false,
            isOnboarded = false
        )
    )

    private val _userInfoPreferences = MutableStateFlow(
        UserInfoPreferences(
            height = 170,
            weight = 70
        )
    )

    private val _goalPreferences = MutableStateFlow(
        GoalPreferences(
            stepGoal = 10000,
            waterGlassGoal = 8,
            sleepGoal = 8,
            exerciseGoal = 300
        )
    )

    private val _dietSelection = MutableStateFlow("Vegetarian")

    // Error simulation flags
    private var shouldThrowException = false
    private var returnIncompleteData = false

    // Methods to configure test data
    fun setUserStateInfo(userStateInfo: UserStateInfo) {
        _userStateInfo.value = userStateInfo
    }

    fun setUserInfoPreferences(userInfoPreferences: UserInfoPreferences) {
        _userInfoPreferences.value = userInfoPreferences
    }

    fun setGoalPreferences(goalPreferences: GoalPreferences) {
        _goalPreferences.value = goalPreferences
    }

    fun setDietSelection(diet: String) {
        _dietSelection.value = diet
    }

    // Error simulation methods
    fun setShouldThrowException(value: Boolean) {
        shouldThrowException = value
    }

    fun setReturnIncompleteData(value: Boolean) {
        returnIncompleteData = value
    }

    // Methods to simulate corrupt or incomplete data
    fun setCorruptGoalPreferences() {
        _goalPreferences.value = GoalPreferences(-999, -999, -999, -999)
    }

    fun setMissingUserData() {
        if (returnIncompleteData) {
            _userStateInfo.value = UserStateInfo(
                uid = "",
                userName = "",
                isUserLoggedIn = false,
                isOnboarded = false
            )
        }
    }

    fun setPartialGoalPreferences() {
        _goalPreferences.value = GoalPreferences(
            stepGoal = 0,
            waterGlassGoal = 0,
            sleepGoal = 0,
            exerciseGoal = 0
        )
    }

    // UserProfileDataSource implementation with error simulation
    override val userStateInfo: Flow<UserStateInfo> = _userStateInfo.map {
        if (shouldThrowException) throw Exception("Test exception in userStateInfo")
        it
    }

    override val userInfoPreferences: Flow<UserInfoPreferences> = _userInfoPreferences.map {
        if (shouldThrowException) throw Exception("Test exception in userInfoPreferences")
        it
    }

    override val goalPreferences: Flow<GoalPreferences> = _goalPreferences.map {
        if (shouldThrowException) throw Exception("Test exception in goalPreferences")
        it
    }

    override val stepsGoal: Flow<Int> = _goalPreferences.map {
        if (shouldThrowException) throw Exception("Test exception in stepsGoal")
        it.stepGoal
    }

    override val sleepGoal: Flow<Int> = _goalPreferences.map {
        if (shouldThrowException) throw Exception("Test exception in sleepGoal")
        it.sleepGoal
    }

    override val waterGoal: Flow<Int> = _goalPreferences.map {
        if (shouldThrowException) throw Exception("Test exception in waterGoal")
        it.waterGlassGoal
    }

    override val exerciseGoal: Flow<Int> = _goalPreferences.map {
        if (shouldThrowException) throw Exception("Test exception in exerciseGoal")
        it.exerciseGoal
    }

    override val heightInfo: Flow<Int> = _userInfoPreferences.map {
        if (shouldThrowException) throw Exception("Test exception in heightInfo")
        it.height
    }

    override val weightInfo: Flow<Int> = _userInfoPreferences.map {
        if (shouldThrowException) throw Exception("Test exception in weightInfo")
        it.weight
    }

    override val dietSelection: Flow<String> = _dietSelection.map {
        if (shouldThrowException) throw Exception("Test exception in dietSelection")
        it
    }

    // Remaining implementations with error simulation
    override suspend fun updateStepGoal(steps: Int) {
        if (shouldThrowException) throw Exception("Test exception in updateStepGoal")
        _goalPreferences.value = _goalPreferences.value.copy(stepGoal = steps)
    }

    override suspend fun updateSleepGoal(sleepHrs: Int) {
        if (shouldThrowException) throw Exception("Test exception in updateSleepGoal")
        _goalPreferences.value = _goalPreferences.value.copy(sleepGoal = sleepHrs)
    }

    override suspend fun updateExerciseGoal(exerciseCal: Int) {
        if (shouldThrowException) throw Exception("Test exception in updateExerciseGoal")
        _goalPreferences.value = _goalPreferences.value.copy(exerciseGoal = exerciseCal)
    }

    override suspend fun updateWaterGlassesGoal(waterGlass: Int) {
        if (shouldThrowException) throw Exception("Test exception in updateWaterGlassesGoal")
        _goalPreferences.value = _goalPreferences.value.copy(waterGlassGoal = waterGlass)
    }

    override suspend fun updateUserHeight(userHeight: Int) {
        if (shouldThrowException) throw Exception("Test exception in updateUserHeight")
        _userInfoPreferences.value = _userInfoPreferences.value.copy(height = userHeight)
    }

    override suspend fun updateUserWeight(userWeight: Int) {
        if (shouldThrowException) throw Exception("Test exception in updateUserWeight")
        _userInfoPreferences.value = _userInfoPreferences.value.copy(weight = userWeight)
    }

    override suspend fun updateDietSelection(dietType: String) {
        if (shouldThrowException) throw Exception("Test exception in updateDietSelection")
        _dietSelection.value = dietType
    }

    override suspend fun updateUserStateInfo(userStateInfo: UserStateInfo) {
        if (shouldThrowException) throw Exception("Test exception in updateUserStateInfo")
        _userStateInfo.value = userStateInfo
    }

    override suspend fun resetOnboardingAndGoalData() {
        if (shouldThrowException) throw Exception("Test exception in resetOnboardingAndGoalData")
        _goalPreferences.value = GoalPreferences(0, 0, 0, 0)
        _userInfoPreferences.value = UserInfoPreferences(168, 60)
        _dietSelection.value = "Vegetarian"
    }
}