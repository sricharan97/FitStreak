package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * Represents the different types of fitness goals that users can track in the application.
 *
 * Each type corresponds to a specific health or fitness activity that can be monitored
 * and for which progress can be measured.
 */
enum class GoalType {
    /**
     * Tracks the number of steps taken by the user during the day.
     * Typically measured by device sensors or connected fitness trackers.
     */
    STEP,

    /**
     * Monitors water consumption to ensure adequate hydration.
     * Usually measured in glasses, milliliters, or ounces consumed.
     */
    WATER,

    /**
     * Records time spent on physical activities and workouts.
     * May include various types of exercises like cardio, strength training, etc.
     */
    EXERCISE,

    /**
     * Tracks sleep duration and potentially quality.
     * Focused on ensuring users get sufficient rest for recovery and overall health.
     */
    SLEEP
}
