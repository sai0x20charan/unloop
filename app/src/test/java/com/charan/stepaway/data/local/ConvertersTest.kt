package com.charan.stepaway.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    @Test
    fun testStringListToString_nullValue_returnsNull() {
        val result = Converters.stringListToString(null)
        assertNull(result)
    }

    @Test
    fun testStringListToString_emptyList_returnsEmptyString() {
        val result = Converters.stringListToString(emptyList())
        assertEquals("", result)
    }

    @Test
    fun testStringListToString_listOfStrings_returnsCommaSeparatedString() {
        val list = listOf("com.example.app1", "com.example.app2")
        val result = Converters.stringListToString(list)
        assertEquals("com.example.app1,com.example.app2", result)
    }

    @Test
    fun testStringToStringList_nullValue_returnsNull() {
        val result = Converters.stringToStringList(null)
        assertNull(result)
    }

    @Test
    fun testStringToStringList_emptyString_returnsEmptyList() {
        val result = Converters.stringToStringList("")
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun testStringToStringList_commaSeparatedString_returnsListOfStrings() {
        val result = Converters.stringToStringList("com.example.app1,com.example.app2")
        val expected = listOf("com.example.app1", "com.example.app2")
        assertEquals(expected, result)
    }
}
