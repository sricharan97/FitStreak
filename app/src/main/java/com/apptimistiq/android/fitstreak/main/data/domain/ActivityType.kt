package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * Represents the different types of activities that can be tracked in the FitStreak application.
 *
 * Each type corresponds to a specific health or fitness activity that users can monitor and
 * for which they can set goals and track progress.
 */
enum class ActivityType {
    /**
     * Represents water consumption tracking.
     */
    WATER,

    /**
     * Represents step count tracking.
     */
    STEP,

    /**
     * Represents sleep duration tracking.
     */
    SLEEP,

    /**
     * Represents physical exercise tracking.
     */
    EXERCISE,

    /**
     * Default activity type used when no specific type is selected.
     */
    DEFAULT
}
