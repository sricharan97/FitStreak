package com.apptimistiq.android.fitstreak.main.data.domain

import com.apptimistiq.android.fitstreak.main.data.GoalPreferences
import com.apptimistiq.android.fitstreak.main.data.UserInfoPreferences

data class GoalUserInfoPreferences(
    val goalPreferences: GoalPreferences,
    val userInfoPreferences: UserInfoPreferences
)

