package com.apptimistiq.android.fitstreak.main.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_store")
data class Activity(
    @ColumnInfo(name = "water_glasses") val waterGlasses: Int,
    @ColumnInfo(name = "sleep_hrs") val sleepHours: Int,
    @ColumnInfo(name = "exercise_cal") val exerciseCalories: Int,
    @ColumnInfo(name = "steps") val steps: Int,
    @ColumnInfo(name = "date_of_activity") val dateOfActivity: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L
)


