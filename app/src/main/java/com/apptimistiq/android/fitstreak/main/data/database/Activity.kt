package com.apptimistiq.android.fitstreak.main.data.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apptimistiq.android.fitstreak.main.data.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType

@Entity(tableName = "activity_store")
data class Activity(
    @NonNull @ColumnInfo(name = "water_glasses") val waterGlasses: Int,
    @NonNull @ColumnInfo(name = "sleep_hrs") val sleepHours: Int,
    @NonNull @ColumnInfo(name = "exercise_cal") val exerciseCalories: Int,
    @NonNull @ColumnInfo(name = "steps") val steps: Int,
    @PrimaryKey @NonNull @ColumnInfo(name = "date_of_activity") val dateOfActivity: Long

)


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
