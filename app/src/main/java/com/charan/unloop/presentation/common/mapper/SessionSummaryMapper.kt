package com.charan.unloop.presentation.common.mapper

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.model.SessionWithSessionEvents
import com.charan.unloop.presentation.common.model.SessionSummaryUiModel

fun SessionWithSessionEvents.toSessionSummaryUiModel() : SessionSummaryUiModel {
    val totalFocusedTime = this.toFocusIntervals()
    return SessionSummaryUiModel(
        id = this.sessionEntity.id,
        sessionName = this.sessionEntity.name,
        focusDurationSeconds = DateTimeUtils.getIntervalDuration(totalFocusedTime).inWholeSeconds.toInt(),
        startTime = this.sessionEntity.createdAt,
        endTime = this.sessionEntity.scheduledEndTime
    )
}

