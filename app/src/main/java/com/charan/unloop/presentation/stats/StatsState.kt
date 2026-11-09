package com.charan.unloop.presentation.stats

import com.charan.unloop.presentation.common.model.SessionSummaryUiModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class StatsState(
    val is24Hour: Boolean = false,
    val totalFocusDurationSeconds: Int = 0,
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEKLY,
    val appBlockCount: Int = 0,
    val focusChartData: List<FocusChartDataUiModel> = emptyList(),
    val selectedDateRange: Pair<LocalDate, LocalDate> = Pair(LocalDate(2024, 6, 1), LocalDate(2024, 6, 7)),
    val sessionCount: Int = 0,
    val recentSessions: List<SessionSummaryUiModel> = emptyList()
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
