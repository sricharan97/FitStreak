package com.apptimistiq.android.fitstreak.utils

/**
 * A sealed class that encapsulates the result of operations that can succeed, fail, or be in progress.
 *
 * This class provides a type-safe way to handle success, error, and loading states,
 * making it easier to propagate operation outcomes through the application layers.
 *
 * @param T The type of data that is returned in case of success
 */
sealed class Result<out T : Any> {

    /**
     * Represents a successful operation with associated data.
     *
     * @property data The data returned by the successful operation
     */
    data class Success<out T : Any>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with an optional error message and status code.
     *
     * @property message A human-readable error message explaining what went wrong
     * @property statusCode An optional HTTP status code or other error code indicating the specific error
     */
    data class Error(val message: String?, val statusCode: Int? = null) : Result<Nothing>()

    /**
     * Represents an operation in progress, optionally containing partial data.
     *
     * @property data Partial data that might be available during loading
     */
    data class Loading<out T : Any>(val data: T) : Result<T>()
}
