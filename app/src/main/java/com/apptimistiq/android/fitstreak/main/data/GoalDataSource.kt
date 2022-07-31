package com.apptimistiq.android.fitstreak.main.data

import kotlinx.coroutines.flow.Flow

interface GoalDataSource {

    val goalPreferences: Flow<GoalPreferences>

    suspend fun updateStepGoal(steps: Int)

    suspend fun updateSleepGoal(sleepHrs: Int)

    suspend fun updateExerciseGoal(exerciseCal: Int)

    suspend fun updateWaterGlassesGoal(waterGlass: Int)

}