package com.charan.unloop.presentation.stats

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class StatsState(
    val totalFocusDurationSeconds: Int = 0,
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEKLY,
    val appBlockCount: Int = 0,
    val focusChartData: List<FocusChartDataUiModel> = emptyList(),
    val selectedDateRange: Pair<LocalDate, LocalDate> = Pair(LocalDate(2024, 6, 1), LocalDate(2024, 6, 7)),
    val sessionCount: Int = 0,
    val recentSessions: List<SessionSummaryUiModel> = emptyList()
)

data class SessionSummaryUiModel(
    val id: String,
    val sessionName: String,
    val focusDurationSeconds: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

enum class StatsPeriod {
    WEEKLY,
    MONTHLY
}

data class FocusChartDataUiModel(
    val focusDurationSeconds: Int = 0,
    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val date: LocalDate = LocalDate(2024, 6, 1)
)
