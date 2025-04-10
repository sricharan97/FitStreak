package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * Enum representing different types of user information and fitness goals that can be edited in the app.
 * 
 * This enum is used to categorize and identify the type of data being modified in user profile
 * and goal-setting screens. It helps maintain type safety when passing information between
 * different components of the application.
 *
 * @see com.apptimistiq.android.fitstreak.main.data.domain
 */
enum class GoalUserInfo {
    // User biometric information
    /**
     * Represents user's height measurement
     * Used in BMI calculations and personalized recommendations
     */
    HEIGHT,
    
    /**
     * Represents user's weight measurement
     * Used in BMI calculations and progress tracking
     */
    WEIGHT,
    
    // Daily fitness goals
    /**
     * Represents daily step count goal
     * Tracks walking and running activities
     */
    STEPS,
    
    /**
     * Represents daily water intake goal
     * Measured in glasses or milliliters
     */
    WATER,
    
    /**
     * Represents daily exercise duration goal
     * Tracks time spent in intentional physical activity
     */
    EXERCISE,
    
    /**
     * Represents daily sleep duration goal
     * Tracks hours of rest for recovery
     */
    SLEEP,
    
    // System states
    /**
     * Default state when no specific information type is selected
     * Used as fallback or initial state
     */
    DEFAULT
}
