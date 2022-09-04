package com.apptimistiq.android.fitstreak.main.data.domain

import com.apptimistiq.android.fitstreak.main.data.database.Activity


enum class ActivityType { WATER, STEP, SLEEP, EXERCISE }

data class ActivityItemUiState(
    val dataType: ActivityType,
    val currentReading: Int = 0,
    val goalReading: Int = 0
    )

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

        }
    }


    return Activity(waterGlassesValue, sleepHrsValue, exerciseCalValue, stepsValue, date)

}