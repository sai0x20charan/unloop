package com.charan.unloop.presentation.session_history

import com.charan.unloop.presentation.common.model.SessionSummaryUiModel
import kotlinx.datetime.LocalDate

data class SessionHistoryState(
    val is24Hour: Boolean = false,
    val dateRange: Pair<LocalDate, LocalDate>? = null,
    val sessions: List<SessionSummaryUiModel> = emptyList(),
)
