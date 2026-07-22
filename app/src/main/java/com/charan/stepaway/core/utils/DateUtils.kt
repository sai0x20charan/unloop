package com.charan.stepaway.core.utils

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock

object DateUtils {
    fun getDaysOfWeek() : List<DayOfWeek> {
        return listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
    }

    fun Month.toLocale() : String{
        return this.toJavaMonth().getDisplayName(
            java.time.format.TextStyle.FULL,
            Locale.getDefault()
        )
    }

    fun DayOfWeek.toLocale(style : TextStyle) : String {
        return this.toJavaDayOfWeek().getDisplayName(
            style,
            Locale.getDefault()
        )
    }

    fun LocalTime.toDisplayString() : String {
        val hour = this.hour
        val minute = this.minute
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
        return String.format("%02d:%02d %s", displayHour, minute, amPm)

    }

    fun currentDateTime() : LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    fun getCurrentTime (): LocalTime {
        return currentDateTime().time
    }
}