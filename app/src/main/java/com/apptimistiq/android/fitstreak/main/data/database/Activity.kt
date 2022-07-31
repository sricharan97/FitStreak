package com.apptimistiq.android.fitstreak.main.data.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityItemUiState
import com.apptimistiq.android.fitstreak.main.data.domain.ActivityType

@Entity(
    tableName = "activity_store",
    indices = [Index(value = ["date_of_activity"], unique = true)]
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @NonNull @ColumnInfo(name = "water_glasses") val waterGlasses: Int = 0,
    @NonNull @ColumnInfo(name = "sleep_hrs") val sleepHours: Int = 0,
    @NonNull @ColumnInfo(name = "exercise_cal") val exerciseCalories: Int = 0,
    @NonNull @ColumnInfo(name = "steps") val steps: Int = 0,
    @NonNull @ColumnInfo(name = "date_of_activity") val dateOfActivity: Long

)


fun Activity.asDomainModel(): List<ActivityItemUiState> {


    return listOf(
        ActivityItemUiState(
            dataType = ActivityType.WATER,
            currentReading = this.waterGlasses
        ),

        ActivityItemUiState(
            dataType = ActivityType.SLEEP,
            currentReading = this.sleepHours
        ),

        ActivityItemUiState(
            dataType = ActivityType.EXERCISE,
            currentReading = this.exerciseCalories
        ),

        ActivityItemUiState(
            dataType = ActivityType.STEP,
            currentReading = this.steps
        )
    )


}
