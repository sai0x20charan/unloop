package com.charan.unloop.presentation.sessions

import com.charan.unloop.presentation.common.model.SessionSummaryUiModel
import kotlinx.datetime.LocalDate

data class SessionsState(
    val dateRange: Pair<LocalDate, LocalDate>? = null,
    val sessions: List<SessionSummaryUiModel> = emptyList(),
)
