package com.charan.stepaway.core.utils

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth
import java.time.format.TextStyle
import java.util.Locale

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
}