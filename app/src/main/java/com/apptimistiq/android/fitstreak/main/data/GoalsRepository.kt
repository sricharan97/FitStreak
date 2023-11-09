package com.apptimistiq.android.fitstreak.main.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


private const val LOG_TAG = "GoalsRepository"

data class GoalPreferences(
    val stepGoal: Int = 0,
    val waterGlassGoal: Int = 0,
    val sleepGoal: Int = 0,
    val exerciseGoal: Int = 0
)

data class UserInfoPreferences(
    val height: Int,
    val weight: Int
)

data class RecipeDietPreferences(
    val dietSelection: String
)

// @Inject tells Dagger how to provide instances of this type
@Singleton
class GoalsRepository @Inject constructor(
    private val goalPreferencesStore: DataStore<Preferences>
) : GoalDataSource {


    //Define PreferenceKeys
    private object PreferenceKeys {
        val STEP_GOAL = intPreferencesKey("steps_goal")
        val WATER_GLASS_GOAL = intPreferencesKey("water_glass_goal")
        val EXERCISE_GOAL = intPreferencesKey("exercise_goal")
        val SLEEP_GOAL = intPreferencesKey("sleep_goal")
        val HEIGHT_INFO = intPreferencesKey("userHeight")
        val WEIGHT_INFO = intPreferencesKey("userWeight")
        val DIET_SELECTION = stringPreferencesKey("diet_selection")
    }

    override val stepsGoal: Flow<Int>
        get() = goalPreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.STEP_GOAL] ?: 0
        }
    override val sleepGoal: Flow<Int>
        get() = goalPreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.SLEEP_GOAL] ?: 0
        }
    override val waterGoal: Flow<Int>
        get() = goalPreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.WATER_GLASS_GOAL] ?: 0
        }
    override val exerciseGoal: Flow<Int>
        get() = goalPreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.EXERCISE_GOAL] ?: 0
        }
    override val heightInfo: Flow<Int>
        get() = goalPreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.HEIGHT_INFO] ?: 168
        }
    override val weightInfo: Flow<Int>
        get() = goalPreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.WEIGHT_INFO] ?: 60
        }

    override val dietSelection: Flow<String>
        get() = goalPreferencesStore.data.map { value: Preferences ->
            value[PreferenceKeys.DIET_SELECTION] ?: "Vegetarian"
        }

    //Expose the goalPreferences as a flow
    override val goalPreferences: Flow<GoalPreferences> = goalPreferencesStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.d(
                    LOG_TAG,
                    "IO Exception occurred while reading from the datastore : ${exception.message}"
                )
                emit(emptyPreferences())
                Log.d(
                    LOG_TAG,
                    "IO Exception occurred while reading from the datastore : ${exception.message}"
                )
            } else {
                Log.d(
                    LOG_TAG,
                    "Exception occurred while reading from the datastore : ${exception.message}"
                )
                throw exception
            }
        }.map { preferences ->

            val steps = preferences[PreferenceKeys.STEP_GOAL] ?: 0
            val sleep = preferences[PreferenceKeys.SLEEP_GOAL] ?: 0
            val water = preferences[PreferenceKeys.WATER_GLASS_GOAL] ?: 0
            val exercise = preferences[PreferenceKeys.EXERCISE_GOAL] ?: 0

            GoalPreferences(steps, water, sleep, exercise)

        }


    override val userInfoPreferences: Flow<UserInfoPreferences> = goalPreferencesStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.d(
                    LOG_TAG,
                    "IO Exception occurred while reading from the datastore : ${exception.message}"
                )
                emit(emptyPreferences())
                Log.d(
                    LOG_TAG,
                    "IO Exception occurred while reading from the datastore : ${exception.message}"
                )
            } else {
                Log.d(
                    LOG_TAG,
                    "Exception occurred while reading from the datastore : ${exception.message}"
                )
                throw exception
            }

        }
        .map { preferences ->
            val height = preferences[PreferenceKeys.HEIGHT_INFO] ?: 168
            val weight = preferences[PreferenceKeys.WEIGHT_INFO] ?: 60

            UserInfoPreferences(height, weight)

        }

    //Store the stepGoal preference
    override suspend fun updateStepGoal(steps: Int) {
        try {
            goalPreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.STEP_GOAL] = steps
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the stepGoal preference")
            throw exception
        }
    }

    //Store the sleepGoal preference
    override suspend fun updateSleepGoal(sleepHrs: Int) {
        try {
            goalPreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.SLEEP_GOAL] = sleepHrs
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the sleepGoal preference")
            throw exception
        }
    }

    //Store the exerciseGoal preference
    override suspend fun updateExerciseGoal(exerciseCal: Int) {
        try {
            goalPreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.EXERCISE_GOAL] = exerciseCal
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the exerciseGoal preference")
            throw exception
        }
    }

    //Store the waterGlassesGoal preference
    override suspend fun updateWaterGlassesGoal(waterGlass: Int) {
        try {
            goalPreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.WATER_GLASS_GOAL] = waterGlass
            }
        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the waterGoal preference")
            throw exception
        }
    }

    override suspend fun updateUserHeight(userHeight: Int) {
        try {
            goalPreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.HEIGHT_INFO] = userHeight

            }

        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the userHeight ")
            throw exception
        }
    }

    override suspend fun updateUserWeight(userWeight: Int) {
        try {
            goalPreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.WEIGHT_INFO] = userWeight

            }

        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the userWeight ")
            throw exception
        }
    }

    override suspend fun updateDietSelection(dietType: String) {
        try {
            goalPreferencesStore.edit { preferences ->
                preferences[PreferenceKeys.DIET_SELECTION] = dietType

            }

        } catch (exception: IOException) {
            Log.e(LOG_TAG, "There is an IO exception while saving the dietSelection ")
            throw exception
        }
    }
}





