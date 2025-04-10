package com.apptimistiq.android.fitstreak.utils

import androidx.databinding.InverseMethod

/**
 * Utility object providing conversion methods for data binding.
 *
 * This object contains methods that enable two-way data binding for 
 * non-String data types in EditText fields, allowing seamless conversion
 * between display text and backing data types.
 */
object Converter {

    /**
     * Converts an integer to its string representation for display in UI elements.
     *
     * Used as the inverse method for [stringToInt] in two-way data binding scenarios.
     *
     * @param value The integer value to convert
     * @return String representation of the integer
     */
    @InverseMethod("stringToInt")
    @JvmStatic
    fun intToString(value: Int): String {
        return value.toString()
    }

    /**
     * Converts a string representation to an integer value.
     *
     * Handles empty strings by returning 0 as a default value.
     *
     * @param value The string value to convert
     * @return The integer representation of the string, or 0 if empty
     */
    @JvmStatic
    fun stringToInt(value: String): Int {
        return if (value.isNotEmpty()) {
            value.toInt()
        } else {
            0
        }
    }
}
