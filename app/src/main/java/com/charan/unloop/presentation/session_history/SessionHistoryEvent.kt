package com.charan.unloop.presentation.session_history

import kotlinx.datetime.LocalDate

sealed interface SessionHistoryEvent {
    data class OnDateRangeChange(val startDate: LocalDate, val endDate: LocalDate) : SessionHistoryEvent
    data object OnBack : SessionHistoryEvent
    data class OnSessionDetails(val id: String) : SessionHistoryEvent
}
