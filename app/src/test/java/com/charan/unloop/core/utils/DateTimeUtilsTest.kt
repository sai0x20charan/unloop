package com.charan.unloop.core.utils

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeUtilsTest {
    @Test
    fun returnsTodayWhenScheduledTimeIsStillAhead() {
        assertEquals(
            LocalDateTime.parse("2026-10-19T10:00"),
            DateTimeUtils.getNextScheduledTime(
                LocalTime(10, 0),
                listOf(DayOfWeek.MONDAY),
                LocalDateTime.parse("2026-10-19T09:00")
            )
        )
    }

    @Test
    fun returnsNextWeekWhenTodaysTimeHasPassedOrIsEqual() {
        for (now in listOf("2026-10-19T10:00", "2026-10-19T11:00")) {
            assertEquals(
                LocalDateTime.parse("2026-10-26T10:00"),
                DateTimeUtils.getNextScheduledTime(
                    LocalTime(10, 0),
                    listOf(DayOfWeek.MONDAY),
                    LocalDateTime.parse(now)
                )
            )
        }
    }

    @Test
    fun findsNearestDayWhenTodayIsNotSelectedAndDaysAreUnordered() {
        assertEquals(
            LocalDateTime.parse("2026-10-21T10:00"),
            DateTimeUtils.getNextScheduledTime(
                LocalTime(10, 0),
                listOf(DayOfWeek.FRIDAY, DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY),
                LocalDateTime.parse("2026-10-19T09:00")
            )
        )
    }

    @Test
    fun selectsAnotherDayWhenTodaysTimeHasPassed() {
        assertEquals(
            LocalDateTime.parse("2026-10-21T10:00"),
            DateTimeUtils.getNextScheduledTime(
                LocalTime(10, 0),
                listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalDateTime.parse("2026-10-19T11:00")
            )
        )
    }

    @Test
    fun rollsOverIntoNextYear() {
        assertEquals(
            LocalDateTime.parse("2027-01-01T08:00"),
            DateTimeUtils.getNextScheduledTime(
                LocalTime(8, 0),
                listOf(DayOfWeek.FRIDAY),
                LocalDateTime.parse("2026-12-31T23:00")
            )
        )
    }

    @Test
    fun preservesScheduledTimePrecision() {
        assertEquals(
            LocalDateTime.parse("2026-10-19T10:00:00.000000002"),
            DateTimeUtils.getNextScheduledTime(
                LocalTime(10, 0, 0, 2),
                listOf(DayOfWeek.MONDAY),
                LocalDateTime.parse("2026-10-19T10:00:00.000000001")
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEmptyFrequency() {
        DateTimeUtils.getNextScheduledTime(
            LocalTime(10, 0),
            emptyList(),
            LocalDateTime.parse("2026-10-19T09:00")
        )
    }
}
