package com.apptimistiq.android.fitstreak.utils

import org.junit.Assert.*
import org.junit.Test

class ConverterTest {

    @Test
    fun `intToString positive integer conversion`() {
        assertEquals("123", Converter.intToString(123))
    }

    @Test
    fun `intToString negative integer conversion`() {
        assertEquals("-123", Converter.intToString(-123))
    }

    @Test
    fun `intToString zero conversion`() {
        assertEquals("0", Converter.intToString(0))
    }

    @Test
    fun `intToString max integer value conversion`() {
        assertEquals(Int.MAX_VALUE.toString(), Converter.intToString(Int.MAX_VALUE))
    }

    @Test
    fun `intToString min integer value conversion`() {
        assertEquals(Int.MIN_VALUE.toString(), Converter.intToString(Int.MIN_VALUE))
    }

    @Test
    fun `stringToInt valid positive integer string`() {
        assertEquals(123, Converter.stringToInt("123"))
    }

    @Test
    fun `stringToInt valid negative integer string`() {
        assertEquals(-123, Converter.stringToInt("-123"))
    }

    @Test
    fun `stringToInt string  0  conversion`() {
        assertEquals(0, Converter.stringToInt("0"))
    }

    @Test
    fun `stringToInt empty string conversion`() {
        assertEquals(0, Converter.stringToInt(""))
    }

    @Test
    fun `stringToInt string with leading zeros`() {
        assertEquals(7, Converter.stringToInt("007"))
    }

    @Test
    fun `stringToInt string representing Int MAX VALUE`() {
        assertEquals(Int.MAX_VALUE, Converter.stringToInt(Int.MAX_VALUE.toString()))
    }

    @Test
    fun `stringToInt string representing Int MIN VALUE`() {
        assertEquals(Int.MIN_VALUE, Converter.stringToInt(Int.MIN_VALUE.toString()))
    }

    @Test(expected = NumberFormatException::class)
    fun `stringToInt non numeric string`() {
        Converter.stringToInt("abc")
    }

    @Test(expected = NumberFormatException::class)
    fun `stringToInt string with spaces`() {
        Converter.stringToInt("123 456")
    }

    @Test(expected = NumberFormatException::class)
    fun `stringToInt string larger than Int MAX VALUE`() {
        Converter.stringToInt("2147483648") // Int.MAX_VALUE + 1
    }

    @Test(expected = NumberFormatException::class)
    fun `stringToInt string smaller than Int MIN VALUE`() {
        Converter.stringToInt("-2147483649") // Int.MIN_VALUE - 1
    }

    @Test
    fun `stringToInt string with plus sign`() {
        assertEquals(123, Converter.stringToInt("+123"))
    }

    @Test(expected = NumberFormatException::class)
    fun `stringToInt string with only a minus sign`() {
        Converter.stringToInt("-")
    }

    @Test(expected = NumberFormatException::class)
    fun `stringToInt string with only a plus sign`() {
        Converter.stringToInt("+")
    }

    @Test
    fun `intToString and stringToInt round trip positive`() {
        val original = 42
        val roundTrip = Converter.stringToInt(Converter.intToString(original))
        assertEquals(original, roundTrip)
    }

    @Test
    fun `intToString and stringToInt round trip negative`() {
        val original = -42
        val roundTrip = Converter.stringToInt(Converter.intToString(original))
        assertEquals(original, roundTrip)
    }

    @Test
    fun `intToString and stringToInt round trip zero`() {
        val original = 0
        val roundTrip = Converter.stringToInt(Converter.intToString(original))
        assertEquals(original, roundTrip)
    }
}