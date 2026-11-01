package com.charan.unloop.presentation.stats

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month

data class StatsState(
    val totalFocusTime : Int = 0,
    val selectedStatsType : StatsType = StatsType.WEEKLY,
    val totalBlockedApps : Int = 0,
    val totalAppUnlocks : Int = 0,
    val graphData : List<GraphData> = emptyList(),
    val graphTimePeriod : Pair<LocalDate, LocalDate> = Pair(LocalDate(2024,6,1),LocalDate(2024,6,7)),
    val totalSessions : Int = 0,
    val recentSessions : List<Sessions> = emptyList()

)

data class Sessions(
    val id : String,
    val sessionName : String,
    val totalTime : Int,
    val startTime : LocalDateTime,
    val endTime : LocalDateTime
)

enum class StatsType{
    WEEKLY,
    MONTHLY
}

data class GraphData(
    val value : Int = 0,
    val day : DayOfWeek = DayOfWeek.MONDAY,
    val date : LocalDate = LocalDate(2024, 6, 1)
)