package com.apptimistiq.android.fitstreak.main.data

import kotlinx.coroutines.flow.Flow

interface GoalDataSource {

    val goalPreferences: Flow<GoalPreferences>

    val userInfoPreferences: Flow<UserInfoPreferences>

    val stepsGoal: Flow<Int>

    val sleepGoal: Flow<Int>

    val waterGoal: Flow<Int>

    val exerciseGoal: Flow<Int>

    val heightInfo: Flow<Int>

    val weightInfo: Flow<Int>

    val dietSelection: Flow<String>


    suspend fun updateStepGoal(steps: Int)

    suspend fun updateSleepGoal(sleepHrs: Int)

    suspend fun updateExerciseGoal(exerciseCal: Int)

    suspend fun updateWaterGlassesGoal(waterGlass: Int)

    suspend fun updateUserHeight(userHeight: Int)

    suspend fun updateUserWeight(userWeight: Int)

    suspend fun updateDietSelection(dietType: String)

}