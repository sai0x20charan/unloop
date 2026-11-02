package com.charan.unloop.presentation.sessions

import kotlinx.datetime.LocalDate

sealed interface SessionsEvent {
    data class OnDateRangeChange(val startDate: LocalDate, val endDate: LocalDate) : SessionsEvent
    data object OnBack : SessionsEvent
}
