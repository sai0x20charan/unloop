package com.charan.unloop.core.utils

import android.content.res.Resources
import com.charan.unloop.R
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.text.DateFormatSymbols
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration
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
        val amPm = DateFormatSymbols.getInstance().amPmStrings[if (hour < 12) 0 else 1]
        val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
        return String.format(locale = Locale.getDefault(),"%02d:%02d %s", displayHour, minute, amPm)
    }

    fun LocalDateTime.toDisplayString(): String = time.toDisplayString()

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

    fun getStartTimeForDuration(endTime: LocalDateTime, seconds: Long): LocalDateTime {
        val timeZone = TimeZone.currentSystemDefault()
        return (endTime.toInstant(timeZone) - seconds.seconds).toLocalDateTime(timeZone)
    }

    fun getDurationSeconds(startTime: LocalDateTime, endTime: LocalDateTime): Long {
        val timeZone = TimeZone.currentSystemDefault()
        return (endTime.toInstant(timeZone) - startTime.toInstant(timeZone)).inWholeSeconds
    }

    fun getRoutineDuration(startTime: LocalTime, endTime: LocalTime): Duration {
        val seconds = endTime.toSecondOfDay() - startTime.toSecondOfDay()
        return (if (seconds <= 0) seconds + 24 * 60 * 60 else seconds).seconds
    }

    fun getScheduledEndTime(
        endTime: LocalTime,
        startTime: LocalDateTime = getCurrentDateTime()
    ): LocalDateTime {
        val scheduledTime = LocalTime(endTime.hour, endTime.minute, endTime.second)
        val candidate = LocalDateTime(startTime.date, scheduledTime)
        return if (candidate <= startTime) {
            LocalDateTime(startTime.date.plus(1, DateTimeUnit.DAY), scheduledTime)
        } else {
            candidate
        }
    }

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

    fun formatHoursAndMinutes(seconds: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 3600, (seconds % 3600) / 60)
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

    fun getNextScheduledTime(
        scheduledTime: LocalTime,
        frequency: List<DayOfWeek>,
        currentDateTime: LocalDateTime = getCurrentDateTime()
    ): LocalDateTime {
        require(frequency.isNotEmpty()) { "Frequency cannot be empty" }

        for (daysToAdd in 0..7) {
            val date = currentDateTime.date.plus(daysToAdd, DateTimeUnit.DAY)
            val candidate = LocalDateTime(date, scheduledTime)

            if (date.dayOfWeek in frequency && candidate > currentDateTime) {
                return candidate
            }
        }

        error("No scheduled time found within the next week")
    }

    fun getCurrentWeekPair() : Pair<LocalDate, LocalDate> {
        val currentDate = getCurrentDateTime().date
        val startOfWeek = currentDate.minus(currentDate.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
        return Pair(startOfWeek, endOfWeek)
    }
    fun getCurrentMonthPair() : Pair<LocalDate, LocalDate> {
        val currentDate = getCurrentDateTime().date
        val startOfMonth = LocalDate(currentDate.year, currentDate.month, 1)
        val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        return Pair(startOfMonth, endOfMonth)
    }

    fun Pair<LocalDate, LocalDate>.getPreviousWeek() : Pair<LocalDate, LocalDate>{
        val startOfWeek = this.first.minus(7, DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
        return Pair(startOfWeek, endOfWeek)

    }
    fun Pair<LocalDate, LocalDate>.getNextWeek() : Pair<LocalDate, LocalDate>{
        val startOfWeek = this.first.plus(7, DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
        return Pair(startOfWeek, endOfWeek)

    }

    fun Pair<LocalDate, LocalDate>.getPreviousMonth() : Pair<LocalDate, LocalDate>{
        val startOfMonth = this.first.minus(1, DateTimeUnit.MONTH)
        val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        return Pair(startOfMonth, endOfMonth)

    }
    fun Pair<LocalDate, LocalDate>.getNextMonth() : Pair<LocalDate, LocalDate>{
        val startOfMonth = this.first.plus(1, DateTimeUnit.MONTH)
        val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        return Pair(startOfMonth, endOfMonth)

    }

    fun LocalDate.atStartOfDay() : LocalDateTime {
        return LocalDateTime(this, LocalTime(0,0,0))
    }
    fun LocalDate.atEndOfDay() : LocalDateTime {
        return LocalDateTime(this, LocalTime(23,59,59))
    }

    fun getDatesInRange(range: Pair<LocalDate, LocalDate>): List<LocalDate> {
        return generateSequence(range.first) { it.plus(1, DateTimeUnit.DAY) }
            .takeWhile { it <= range.second }
            .toList()
    }

    fun getIntervalDuration(
        intervals: List<Pair<LocalDateTime, LocalDateTime>>,

        ) : Duration{
        val timeZone = TimeZone.currentSystemDefault()

        var totalDuration = Duration.ZERO
        intervals.forEach { (start, end) ->
            totalDuration += end.toInstant(timeZone) - start.toInstant(timeZone)
        }
        return totalDuration

    }

    fun getDailyDurations(
        intervals: List<Pair<LocalDateTime, LocalDateTime>>,
        range: Pair<LocalDate, LocalDate>
    ): Map<LocalDate, Int> {
        val timeZone = TimeZone.currentSystemDefault()
        val rangeStart = range.first.atStartOfDayIn(timeZone)
        val rangeEnd = range.second.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        val durations = mutableMapOf<LocalDate, Duration>()

        intervals.forEach { (start, end) ->
            var intervalStart = maxOf(start.toInstant(timeZone), rangeStart)
            val intervalEnd = minOf(end.toInstant(timeZone), rangeEnd)

            while (intervalStart < intervalEnd) {
                val date = intervalStart.toLocalDateTime(timeZone).date
                val nextDayStart = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
                val dayEnd = minOf(intervalEnd, nextDayStart)
                durations[date] = (durations[date] ?: Duration.ZERO) + (dayEnd - intervalStart)
                intervalStart = dayEnd
            }
        }

        return durations.mapValues { (_, duration) -> duration.inWholeSeconds.toInt() }
    }

    fun LocalTime.toSeconds() : Int {
        return this.hour * 3600 + this.minute * 60 + this.second
    }
    fun Pair<LocalDate, LocalDate>.toDisplayString() : String{
        return "${this.first.month.toLocale()} ${this.first.dayOfMonth} - ${this.second.month.toLocale()} ${this.second.dayOfMonth}"
    }

    fun Int.toFocusDuration(resources: Resources): String {
        val seconds = coerceAtLeast(0)
        return if (seconds >= 3600) {
            resources.getString(
                R.string.stats_duration_hours_minutes,
                seconds / 3600,
                (seconds % 3600) / 60
            )
        } else {
            resources.getString(
                R.string.stats_duration_minutes_seconds,
                seconds / 60,
                seconds % 60
            )
        }
    }

    fun Int.toFocusHours(resources: Resources) : String{
        return resources.getString(
            R.string.stats_duration_hours,
            coerceAtLeast(0) / 3600
        )
    }

    fun DayOfWeek.getDisplayName(textStyle : TextStyle) : String{
        return this.toJavaDayOfWeek().getDisplayName(textStyle, Locale.getDefault())
    }


    }

