package com.apptimistiq.android.fitstreak.main.data.domain

data class GoalPreferences(
    val stepGoal: Int = 0,
    val waterGlassGoal: Int = 0,
    val sleepGoal: Int = 0,
    val exerciseGoal: Int = 0
)

data class UserInfoPreferences(
    val height: Int,
    val weight: Int
)

data class UserStateInfo(
    val userName: String = "User",
    val isUserLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false
)

