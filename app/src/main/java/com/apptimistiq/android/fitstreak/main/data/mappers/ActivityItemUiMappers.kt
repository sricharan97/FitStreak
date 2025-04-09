package com.apptimistiq.android.fitstreak.main.data.mappers

import com.apptimistiq.android.fitstreak.main.data.database.Activity
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType

fun List<ActivityItemUiState>.asDatabaseModel(date: Long): Activity {

    var stepsValue = 0
    var waterGlassesValue = 0
    var exerciseCalValue = 0
    var sleepHrsValue = 0

    this.forEach {
        when (it.dataType) {
            ActivityType.STEP -> {
                stepsValue = it.currentReading
            }
            ActivityType.SLEEP -> {
                sleepHrsValue = it.currentReading
            }
            ActivityType.EXERCISE -> {
                exerciseCalValue = it.currentReading
            }
            ActivityType.WATER -> {
                waterGlassesValue = it.currentReading
            }

            ActivityType.DEFAULT -> { /* Default case - no specific value to set */ }
        }
    }


    return Activity(
        waterGlasses = waterGlassesValue,
        sleepHours = sleepHrsValue,
        exerciseCalories = exerciseCalValue,
        steps = stepsValue,
        dateOfActivity = date
    )

}