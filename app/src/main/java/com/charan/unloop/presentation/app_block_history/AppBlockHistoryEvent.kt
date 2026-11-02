package com.charan.unloop.presentation.app_block_history

import kotlinx.datetime.LocalDate

sealed interface AppBlockHistoryEvent {
    data class OnDateRangeChange(val startDate: LocalDate, val endDate: LocalDate) : AppBlockHistoryEvent
    data object OnBack : AppBlockHistoryEvent
}
