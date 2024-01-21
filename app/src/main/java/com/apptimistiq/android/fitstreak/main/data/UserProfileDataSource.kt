package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserInfoPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.flow.Flow

interface UserProfileDataSource {

    val goalPreferences: Flow<GoalPreferences>

    val userInfoPreferences: Flow<UserInfoPreferences>

    val userStateInfo: Flow<UserStateInfo>

    val stepsGoal: Flow<Int>

    val sleepGoal: Flow<Int>

    val waterGoal: Flow<Int>

    val exerciseGoal: Flow<Int>

    val heightInfo: Flow<Int>

    val weightInfo: Flow<Int>

    val dietSelection: Flow<String>

    val userLoggedIn: Flow<Boolean>

    val userName: Flow<String>

    val userOnboarded: Flow<Boolean>


    suspend fun updateStepGoal(steps: Int)

    suspend fun updateSleepGoal(sleepHrs: Int)

    suspend fun updateExerciseGoal(exerciseCal: Int)

    suspend fun updateWaterGlassesGoal(waterGlass: Int)

    suspend fun updateUserHeight(userHeight: Int)

    suspend fun updateUserWeight(userWeight: Int)

    suspend fun updateDietSelection(dietType: String)

    suspend fun updateUserLoggedIn(userLoggedIn: Boolean)

    suspend fun updateUserName(userName: String)

    suspend fun updateUserOnboarded(userOnboarded: Boolean)

}