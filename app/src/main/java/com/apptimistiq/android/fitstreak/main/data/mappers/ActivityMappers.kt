package com.apptimistiq.android.fitstreak.main.data.mappers

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType
import com.apptimistiq.android.fitstreak.main.data.domain.GoalPreferences

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

fun Activity.asActivityTypeVal(activityType: ActivityType): Int {

    when (activityType) {
        ActivityType.STEP -> {
            return this.steps
        }
        ActivityType.WATER -> {
            return this.waterGlasses
        }
        ActivityType.EXERCISE -> {
            return this.exerciseCalories
        }
        ActivityType.SLEEP -> {
            return this.sleepHours
        }
        ActivityType.DEFAULT -> {
            return 0
        }
    }
}