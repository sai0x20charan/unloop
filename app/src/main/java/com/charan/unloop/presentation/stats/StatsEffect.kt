package com.charan.unloop.presentation.stats

import kotlinx.datetime.LocalDate

sealed interface StatsEffect {
    data class NavigateToSessionHistory(val startDate: LocalDate, val endDate: LocalDate) : StatsEffect
    data class NavigateToAppBlockHistory(val startDate: LocalDate, val endDate: LocalDate) : StatsEffect
    data class NavigateToSessionDetails(val id : String) : StatsEffect
}
