package com.charan.unloop.data.local

import com.charan.unloop.data.local.model.enums.ScheduleType
import com.charan.unloop.data.local.model.enums.SessionEvents
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

    @Test
    fun testSessionEventsToString_and_stringToSessionEvents() {
        SessionEvents.entries.forEach { event ->
            val stringVal = Converters.sessionEventsToString(event)
            assertEquals(event.name, stringVal)
            val convertedBack = Converters.stringToSessionEvents(stringVal)
            assertEquals(event, convertedBack)
        }
    }

    @Test
    fun testScheduleTypeToString_and_stringToScheduleType() {
        ScheduleType.entries.forEach { type ->
            val stringVal = Converters.scheduleTypeToString(type)
            assertEquals(type.name, stringVal)
            val convertedBack = Converters.stringToScheduleType(stringVal)
            assertEquals(type, convertedBack)
        }
    }
}
