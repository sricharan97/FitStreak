package com.apptimistiq.android.fitstreak.main.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserInfoPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.UserStateInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logger tag for UserProfileRepository
 */
private const val LOG_TAG = "UserProfileRepository"

/**
 * Data class representing user's diet preferences
 *
 * @property dietSelection The user's selected diet type
 */
data class RecipeDietPreferences(
    val dietSelection: String
)

/**
 * Repository implementation for managing user profile data.
 * This class handles storing and retrieving user preferences using DataStore.
 * It implements the UserProfileDataSource interface to provide a consistent API.
 *
 * @property userProfilePreferencesStore DataStore used for storing user preferences
 */
@Singleton
class UserProfileRepository @Inject constructor(
    private val userProfilePreferencesStore: DataStore<Preferences>
) : UserProfileDataSource {

    /**
     * Preference keys used for storing and retrieving data from DataStore
     */
    private object PreferenceKeys {
        val STEP_GOAL = intPreferencesKey("steps_goal")
        val WATER_GLASS_GOAL = intPreferencesKey("water_glass_goal")
        val EXERCISE_GOAL = intPreferencesKey("exercise_goal")
        val SLEEP_GOAL = intPreferencesKey("sleep_goal")
        val HEIGHT_INFO = intPreferencesKey("userHeight")
        val WEIGHT_INFO = intPreferencesKey("userWeight")
        val DIET_SELECTION = stringPreferencesKey("diet_selection")
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_LOGGED_IN = booleanPreferencesKey("user_logged_in")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ONBOARDED = booleanPreferencesKey("user_onboarded")
    }

    /**
     * Returns a Flow of the user's daily step goal
     * @return Flow<Int> containing step goal or 0 if not set
     */
    override val stepsGoal: Flow<Int>
        get() = userProfilePreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.STEP_GOAL] ?: 0
        }

    /**
     * Returns a Flow of the user's daily sleep goal
     * @return Flow<Int> containing sleep goal in hours or 0 if not set
     */
    override val sleepGoal: Flow<Int>
        get() = userProfilePreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.SLEEP_GOAL] ?: 0
        }

    /**
     * Returns a Flow of the user's daily water intake goal
     * @return Flow<Int> containing water goal in glasses or 0 if not set
     */
    override val waterGoal: Flow<Int>
        get() = userProfilePreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.WATER_GLASS_GOAL] ?: 0
        }

    /**
     * Returns a Flow of the user's daily exercise goal
     * @return Flow<Int> containing exercise goal in calories or 0 if not set
     */
    override val exerciseGoal: Flow<Int>
        get() = userProfilePreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.EXERCISE_GOAL] ?: 0
        }

    /**
     * Returns a Flow of the user's height information
     * @return Flow<Int> containing user height or 168cm default
     */
    override val heightInfo: Flow<Int>
        get() = userProfilePreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.HEIGHT_INFO] ?: 168
        }

    /**
     * Returns a Flow of the user's weight information
     * @return Flow<Int> containing user weight or 60kg default
     */
    override val weightInfo: Flow<Int>
        get() = userProfilePreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.WEIGHT_INFO] ?: 60
        }

    /**
     * Returns a Flow of the user's diet selection
     * @return Flow<String> containing diet preference or "Vegetarian" default
     */
    override val dietSelection: Flow<String>
        get() = userProfilePreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.DIET_SELECTION] ?: "Vegetarian"
        }

    /**
     * Returns a Flow of all user goal preferences combined in a data class
     * Handles IO exceptions that might occur when reading from DataStore
     * 
     * @return Flow<GoalPreferences> containing all user goals
     */
    override val goalPreferences: Flow<GoalPreferences> = userProfilePreferencesStore.data
        .catch { exception ->
            handleDataStoreException(exception, "reading goal preferences")
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val steps = preferences[PreferenceKeys.STEP_GOAL] ?: 0
            val sleep = preferences[PreferenceKeys.SLEEP_GOAL] ?: 0
            val water = preferences[PreferenceKeys.WATER_GLASS_GOAL] ?: 0
            val exercise = preferences[PreferenceKeys.EXERCISE_GOAL] ?: 0

            GoalPreferences(steps, water, sleep, exercise)
        }

    /**
     * Returns a Flow of user profile information combined in a data class
     * Handles IO exceptions that might occur when reading from DataStore
     * 
     * @return Flow<UserInfoPreferences> containing height and weight information
     */
    override val userInfoPreferences: Flow<UserInfoPreferences> = userProfilePreferencesStore.data
        .catch { exception ->
            handleDataStoreException(exception, "reading user info preferences")
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val height = preferences[PreferenceKeys.HEIGHT_INFO] ?: 168
            val weight = preferences[PreferenceKeys.WEIGHT_INFO] ?: 60

            UserInfoPreferences(height, weight)
        }

    /**
     * Returns a Flow of user state information combined in a data class
     * Handles IO exceptions that might occur when reading from DataStore
     * 
     * @return Flow<UserStateInfo> containing login state and user info
     */
    override val userStateInfo: Flow<UserStateInfo>
        get() = userProfilePreferencesStore.data
            .catch { exception ->
                handleDataStoreException(exception, "reading user state info")
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { value: Preferences ->
                val userLoggedIn = value[PreferenceKeys.USER_LOGGED_IN] ?: false
                val userName = value[PreferenceKeys.USER_NAME] ?: "User"
                val userOnboarded = value[PreferenceKeys.USER_ONBOARDED] ?: false
                val uid = value[PreferenceKeys.USER_UID] ?: ""
                UserStateInfo(
                    uid = uid,
                    userName = userName,
                    isUserLoggedIn = userLoggedIn,
                    isOnboarded = userOnboarded
                )
            }

    /**
     * Updates the user's daily step goal
     * 
     * @param steps The new step goal to save
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun updateStepGoal(steps: Int) {
        try {
            userProfilePreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.STEP_GOAL] = steps
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the stepGoal preference")
            throw exception
        }
    }

    /**
     * Updates the user's daily sleep goal
     * 
     * @param sleepHrs The new sleep goal in hours to save
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun updateSleepGoal(sleepHrs: Int) {
        try {
            userProfilePreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.SLEEP_GOAL] = sleepHrs
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the sleepGoal preference")
            throw exception
        }
    }

    /**
     * Updates the user's daily exercise goal
     * 
     * @param exerciseCal The new exercise goal in calories to save
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun updateExerciseGoal(exerciseCal: Int) {
        try {
            userProfilePreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.EXERCISE_GOAL] = exerciseCal
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the exerciseGoal preference")
            throw exception
        }
    }

    /**
     * Updates the user's daily water intake goal
     * 
     * @param waterGlass The new water goal in glasses to save
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun updateWaterGlassesGoal(waterGlass: Int) {
        try {
            userProfilePreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.WATER_GLASS_GOAL] = waterGlass
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the waterGoal preference")
            throw exception
        }
    }

    /**
     * Updates the user's height information
     * 
     * @param userHeight The new user height in cm
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun updateUserHeight(userHeight: Int) {
        try {
            userProfilePreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.HEIGHT_INFO] = userHeight
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the userHeight")
            throw exception
        }
    }

    /**
     * Updates the user's weight information
     * 
     * @param userWeight The new user weight in kg
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun updateUserWeight(userWeight: Int) {
        try {
            userProfilePreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.WEIGHT_INFO] = userWeight
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the userWeight")
            throw exception
        }
    }

    /**
     * Updates the user's diet preference
     * 
     * @param dietType The new diet preference as a string
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun updateDietSelection(dietType: String) {
        try {
            userProfilePreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.DIET_SELECTION] = dietType
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the dietSelection")
            throw exception
        }
    }

    /**
     * Updates the user's state information including login status
     * 
     * @param userStateInfo The new user state information
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun updateUserStateInfo(userStateInfo: UserStateInfo) {
        try {
            userProfilePreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.USER_LOGGED_IN] = userStateInfo.isUserLoggedIn
                preferences[PreferenceKeys.USER_NAME] = userStateInfo.userName
                preferences[PreferenceKeys.USER_ONBOARDED] = userStateInfo.isOnboarded
                preferences[PreferenceKeys.USER_UID] = userStateInfo.uid
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the userStateInfo")
            throw exception
        }
    }

    /**
     * Resets all onboarding data and user-specific preferences to their default state.
     *
     * @throws IOException if there's an error writing to DataStore
     */
    override suspend fun resetOnboardingAndGoalData() {
        try {
            userProfilePreferencesStore.edit { preferences ->
                // Reset goals
                preferences[PreferenceKeys.STEP_GOAL] = 0
                preferences[PreferenceKeys.WATER_GLASS_GOAL] = 0
                preferences[PreferenceKeys.EXERCISE_GOAL] = 0
                preferences[PreferenceKeys.SLEEP_GOAL] = 0
                // Reset user info
                preferences[PreferenceKeys.HEIGHT_INFO] = 168 // Default height
                preferences[PreferenceKeys.WEIGHT_INFO] = 60  // Default weight
                preferences[PreferenceKeys.DIET_SELECTION] = "Vegetarian" // Default diet
                // Note: USER_LOGGED_IN, USER_NAME, USER_ONBOARDED, USER_UID
                // will be handled by a separate call to updateUserStateInfo after sign-out.
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while resetting onboarding and goal data")
            throw exception
        }
    }

    /**
     * Helper function to handle exceptions from DataStore operations
     * 
     * @param exception The exception that occurred
     * @param operation Description of the operation that failed
     */
    private fun handleDataStoreException(exception: Throwable, operation: String) {
        if (exception is IOException) {
            Log.d(LOG_TAG, "IO Exception occurred while $operation: ${exception.message}")
        } else {
            Log.d(LOG_TAG, "Exception occurred while $operation: ${exception.message}")
        }
    }
}
