package com.apptimistiq.android.fitstreak.utils

import androidx.databinding.InverseMethod


/**
 * Converter object to be used for two way data binding for non-String data in edit text fields
 */
object Converter {

    @InverseMethod("stringToInt")
    @JvmStatic
    fun intToString(value: Int): String {

        return value.toString()

    }

    @JvmStatic
    fun stringToInt(value: String): Int {
        return if (value.isNotEmpty()) {
            value.toInt()
        } else {
            "0".toInt()
        }

    }
}