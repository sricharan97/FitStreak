package com.apptimistiq.android.fitstreak.main.data.mappers

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences

/**
 * Extension function to convert a database [Activity] entity into a list of domain model [ActivityItemUiState] objects,
 * comparing current readings with user goals from [GoalPreferences].
 *
 * @param goalPreferences The user's goal preferences containing target values for each activity type
 * @return A list of [ActivityItemUiState] objects representing each activity type with current and goal readings
 */
fun Activity.asDomainModel(goalPreferences: GoalPreferences): List<ActivityItemUiState> {
    return listOf(
        ActivityItemUiState(
            dataType = ActivityType.WATER,
            currentReading = this.waterGlasses,
            goalReading = goalPreferences.waterGlassGoal
        ),
        ActivityItemUiState(
            dataType = ActivityType.SLEEP,
            currentReading = this.sleepHours,
            goalReading = goalPreferences.sleepGoal
        ),
        ActivityItemUiState(
            dataType = ActivityType.EXERCISE,
            currentReading = this.exerciseCalories,
            goalReading = goalPreferences.exerciseGoal
        ),
        ActivityItemUiState(
            dataType = ActivityType.STEP,
            currentReading = this.steps,
            goalReading = goalPreferences.stepGoal
        )
    )
}

/**
 * Extension function to extract a specific activity metric value based on the provided activity type.
 *
 * @param activityType The type of activity metric to retrieve
 * @return The current integer value of the specified activity metric from the database entity
 */
fun Activity.asActivityTypeVal(activityType: ActivityType): Int {
    return when (activityType) {
        ActivityType.STEP -> this.steps
        ActivityType.WATER -> this.waterGlasses
        ActivityType.EXERCISE -> this.exerciseCalories
        ActivityType.SLEEP -> this.sleepHours
        ActivityType.DEFAULT -> 0
    }
}
