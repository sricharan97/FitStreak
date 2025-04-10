package com.apptimistiq.android.fitstreak.main.data.mappers

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType

/**
 * ActivityItemUiMappers.kt
 * 
 * Provides mapping functions to convert between UI state models and database entities
 * for activity tracking data in the FitStreak application.
 */

/**
 * Converts a list of UI activity items into a single database entity.
 *
 * @param date The timestamp representing the date for which these activities are recorded
 * @return An [Activity] database entity populated with values from the UI state
 */
fun List<ActivityItemUiState>.asDatabaseModel(date: Long): Activity {
    // Initialize default values for all activity metrics
    var steps = 0
    var waterGlasses = 0
    var exerciseCalories = 0
    var sleepHours = 0

    // Extract values from each UI state item based on activity type
    this.forEach { activityItem ->
        when (activityItem.dataType) {
            ActivityType.STEP -> steps = activityItem.currentReading
            ActivityType.SLEEP -> sleepHours = activityItem.currentReading
            ActivityType.EXERCISE -> exerciseCalories = activityItem.currentReading
            ActivityType.WATER -> waterGlasses = activityItem.currentReading
            ActivityType.DEFAULT -> { /* No specific value to set for default type */ }
        }
    }

    // Create and return the database entity with all collected values
    return Activity(
        waterGlasses = waterGlasses,
        sleepHours = sleepHours,
        exerciseCalories = exerciseCalories,
        steps = steps,
        dateOfActivity = date
    )
}
