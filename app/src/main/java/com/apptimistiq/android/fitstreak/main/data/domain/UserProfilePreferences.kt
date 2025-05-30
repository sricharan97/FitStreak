/**
 * FitStreak - User Profile Preferences Domain Models
 *
 * This file contains data classes that represent various user preferences and states
 * used throughout the FitStreak application. These models serve as the domain layer
 * representation of user data.
 *
 * @author Apptimistiq
 * @version 1.0
 */
package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * Represents the user's fitness and health goals.
 *
 * @property stepGoal Daily step count target, default is 0 (unset)
 * @property waterGlassGoal Daily water consumption target in glasses, default is 0 (unset)
 * @property sleepGoal Daily sleep target in hours, default is 0 (unset)
 * @property exerciseGoal Daily exercise target in minutes, default is 0 (unset)
 */
data class GoalPreferences(
    val stepGoal: Int = 0,
    val waterGlassGoal: Int = 0,
    val sleepGoal: Int = 0,
    val exerciseGoal: Int = 0
)

/**
 * Represents the user's physical attributes.
 *
 * @property height User's height in centimeters
 * @property weight User's weight in kilograms
 */
data class UserInfoPreferences(
    val height: Int,
    val weight: Int
)

/**
 * Represents the user's state information within the application.
 *
 * @property userName User's display name, defaults to "User"
 * @property isUserLoggedIn Indicates whether the user is currently logged in
 * @property isOnboarded Indicates whether the user has completed the onboarding process
 */
data class UserStateInfo(
    val uid: String = "", //Firebase UID
    val userName: String = "User",
    val isUserLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false
)
