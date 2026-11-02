package com.charan.unloop.presentation.common.model

import kotlinx.datetime.LocalDateTime

data class SessionSummaryUiModel(
    val id: String,
    val sessionName: String,
    val focusDurationSeconds: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
