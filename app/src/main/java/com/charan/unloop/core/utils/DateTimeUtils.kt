package com.charan.unloop.core.utils

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds


object DateTimeUtils {

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
            TextStyle.FULL,
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
        return String.format(locale = Locale.getDefault(),"%02d:%02d %s", displayHour, minute, amPm)
    }

    fun LocalDateTime.toDisplayString() : String {
        val hour = this.hour
        val minute = this.minute
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
        return String.format(locale = Locale.getDefault(),"%02d:%02d %s", displayHour, minute, amPm)
    }

    fun getCurrentDateTime() : LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    fun getCurrentTime (): LocalTime {
        return getCurrentDateTime().time
    }

    fun getEndTimeForDuration(seconds: Long): LocalDateTime {
        val endInstant = Clock.System.now() + seconds.seconds
        return endInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    fun getEndTimeForDuration(seconds: Int): LocalDateTime = getEndTimeForDuration(seconds.toLong())

    fun getRemainingDuration(endTime: LocalDateTime): Long {
        val endInstant = endTime.toInstant(TimeZone.currentSystemDefault())
        val currentInstant = Clock.System.now()

        return (endInstant - currentInstant)
            .inWholeSeconds
            .coerceAtLeast(0L)
    }

    fun LocalTime.toLocalDateTime() : LocalDateTime {
        val currentDate = getCurrentDateTime().date
        return LocalDateTime(
            year = currentDate.year,
            month = currentDate.month,
            day = currentDate.day,
            hour = this.hour,
            minute = this.minute,
            second = this.second,
            nanosecond = 0
        )
    }

    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format(Locale.getDefault(),"%02d:%02d", minutes, remainingSeconds)
        }
    }
}
