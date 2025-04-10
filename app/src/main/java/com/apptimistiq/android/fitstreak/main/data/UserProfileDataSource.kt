package com.apptimistiq.android.fitstreak.main.data

import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserInfoPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining data source operations for user profile management.
 * 
 * This interface provides methods to access and update various user profile information
 * including fitness goals, personal metrics, and preferences.
 */
interface UserProfileDataSource {

    // ===== User information flows =====
    
    /**
     * Flow of user's state information including activity status, 
     * motivation level, and other tracking state data.
     */
    val userStateInfo: Flow<UserStateInfo>
    
    /**
     * Flow of user's personal information preferences.
     */
    val userInfoPreferences: Flow<UserInfoPreferences>
    
    /**
     * Flow of user's goal preference settings.
     */
    val goalPreferences: Flow<GoalPreferences>

    // ===== Fitness goals flows =====
    
    /**
     * Flow of user's daily step count goal.
     * @return A Flow emitting the target number of steps.
     */
    val stepsGoal: Flow<Int>
    
    /**
     * Flow of user's daily sleep goal.
     * @return A Flow emitting the target number of sleep hours.
     */
    val sleepGoal: Flow<Int>
    
    /**
     * Flow of user's daily water intake goal.
     * @return A Flow emitting the target number of water glasses.
     */
    val waterGoal: Flow<Int>
    
    /**
     * Flow of user's daily exercise calorie goal.
     * @return A Flow emitting the target exercise calories.
     */
    val exerciseGoal: Flow<Int>

    // ===== User metrics flows =====
    
    /**
     * Flow of user's height information.
     * @return A Flow emitting the user's height in centimeters.
     */
    val heightInfo: Flow<Int>
    
    /**
     * Flow of user's weight information.
     * @return A Flow emitting the user's weight in kilograms.
     */
    val weightInfo: Flow<Int>
    
    /**
     * Flow of user's dietary preference selection.
     * @return A Flow emitting the user's diet type as a string.
     */
    val dietSelection: Flow<String>

    // ===== Update operations =====
    
    /**
     * Updates the user's daily step goal.
     * @param steps The new target number of steps.
     */
    suspend fun updateStepGoal(steps: Int)

    /**
     * Updates the user's daily sleep goal.
     * @param sleepHrs The new target hours of sleep.
     */
    suspend fun updateSleepGoal(sleepHrs: Int)

    /**
     * Updates the user's daily exercise calorie goal.
     * @param exerciseCal The new target exercise calories.
     */
    suspend fun updateExerciseGoal(exerciseCal: Int)

    /**
     * Updates the user's daily water intake goal.
     * @param waterGlass The new target number of water glasses.
     */
    suspend fun updateWaterGlassesGoal(waterGlass: Int)

    /**
     * Updates the user's height information.
     * @param userHeight The new height value in centimeters.
     */
    suspend fun updateUserHeight(userHeight: Int)

    /**
     * Updates the user's weight information.
     * @param userWeight The new weight value in kilograms.
     */
    suspend fun updateUserWeight(userWeight: Int)

    /**
     * Updates the user's dietary preference.
     * @param dietType The new diet type selection.
     */
    suspend fun updateDietSelection(dietType: String)

    /**
     * Updates the user's state information.
     * @param userStateInfo The new user state information object.
     */
    suspend fun updateUserStateInfo(userStateInfo: UserStateInfo)
}
