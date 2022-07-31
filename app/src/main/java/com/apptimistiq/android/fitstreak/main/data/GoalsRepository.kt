package com.apptimistiq.android.fitstreak.main.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


private const val LOG_TAG = "GoalsRepository"

data class GoalPreferences(
    val stepGoal: Int,
    val waterGlassGoal: Int,
    val sleepGoal: Int,
    val exerciseGoal: Int
)


class GoalsRepository(
    private val goalPreferencesStore: DataStore<Preferences>
) : GoalDataSource {


    //Define PreferenceKeys
    private object PreferenceKeys {
        val STEP_GOAL = intPreferencesKey("steps_goal")
        val WATER_GLASS_GOAL = intPreferencesKey("water_glass_goal")
        val EXERCISE_GOAL = intPreferencesKey("exercise_goal")
        val SLEEP_GOAL = intPreferencesKey("sleep_goal")
    }

    //Expose the goalPreferences as a flow
    override val goalPreferences: Flow<GoalPreferences> = goalPreferencesStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
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


}





