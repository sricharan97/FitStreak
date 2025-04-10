package com.apptimistiq.android.fitstreak.utils

/**
 * A wrapper for data that is exposed via a LiveData that represents a one-time event.
 *
 * This class helps prevent events from being handled multiple times when LiveData
 * observers are reattached due to configuration changes or other lifecycle events.
 * Once the event content has been handled, subsequent observers will not receive it.
 *
 * @param T The type of content this event contains
 * @property content The actual data payload of the event
 */
open class Event<out T>(private val content: T) {

    /**
     * Flag indicating whether this event has been handled.
     *
     * External code can read but not modify this property.
     */
    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     *
     * This method should be used to access event content that should only be handled once,
     * such as navigation events or messages to display in a Snackbar.
     *
     * @return The content if not yet handled, null otherwise
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     *
     * This method should be used when you need to access the content regardless
     * of whether it's been handled, such as for testing or debugging purposes.
     *
     * @return The content of this event
     */
    fun peekContent(): T = content
}
