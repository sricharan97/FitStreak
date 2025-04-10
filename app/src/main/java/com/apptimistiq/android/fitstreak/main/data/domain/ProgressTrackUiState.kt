package com.apptimistiq.android.fitstreak.main.data.domain

/**
 * Represents the UI state for the progress tracking feature of the application.
 * This data class encapsulates all information needed to render the progress tracking screen,
 * including activity data, loading states, user messages, and feature availability flags.
 */
data class ProgressTrackUiState(
    /**
     * List of activities to display in the UI.
     * Each item represents an individual activity with its associated data.
     */
    val activityList: List<ActivityItemUiState> = emptyList(),
    
    /**
     * Indicates whether the app is currently fetching activities from the data source.
     * Used to display loading indicators in the UI.
     */
    val isFetchingActivities: Boolean = false,
    
    /**
     * Optional message to display to the user, such as errors or notifications.
     * Null when there are no messages to show.
     */
    val userMessages: String? = null,
    
    /**
     * Flag indicating whether the app has permission to access Google Fit data.
     */
    val canAccessGoogleFit: Boolean = false,
    
    /**
     * Flag indicating whether the user has completed the subscription process.
     */
    val subscriptionDone: Boolean = false,
    
    /**
     * Flags indicating which health metrics the app has permission to read.
     * These determine which features are available to the user.
     */
    val readSteps: Boolean = false,
    val readCalories: Boolean = false,
    val readWaterLitres: Boolean = false,
    val readSleepHrs: Boolean = false,
    
    /**
     * Flag indicating whether activity data has been saved for the current day.
     */
    val activitySavedForDay: Boolean = false
)
