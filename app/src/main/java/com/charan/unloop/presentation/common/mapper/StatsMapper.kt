package com.charan.unloop.presentation.common.mapper

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.SessionWithSessionEvents
import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.presentation.stats.FocusChartDataUiModel
import com.charan.unloop.presentation.stats.SessionSummaryUiModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

fun List<SessionWithSessionEvents>.toFocusChartDataUiModels(
    range: Pair<LocalDate, LocalDate>,
    currentDateTime: LocalDateTime = DateTimeUtils.getCurrentDateTime()
): List<FocusChartDataUiModel> {
    val intervals = flatMap { it.toFocusIntervals(currentDateTime) }
    val durations = DateTimeUtils.getDailyDurations(intervals, range)

    return DateTimeUtils.getDatesInRange(range).map { date ->
        FocusChartDataUiModel(
            focusDurationSeconds = durations[date] ?: 0,
            dayOfWeek = date.dayOfWeek,
            date = date
        )
    }
}

fun SessionWithSessionEvents.toSessionSummaryUiModel() : SessionSummaryUiModel {
    val totalFocusedTime = this.toFocusIntervals()
    return SessionSummaryUiModel(
        id = this.sessionEntity.id,
        sessionName = this.sessionEntity.sessionName,
        focusDurationSeconds = DateTimeUtils.getIntervalDuration(totalFocusedTime).inWholeSeconds.toInt(),
        startTime = this.sessionEntity.createdAt,
        endTime = this.sessionEntity.endTime
    )
}

private fun SessionWithSessionEvents.toFocusIntervals(
    currentDateTime: LocalDateTime = DateTimeUtils.getCurrentDateTime()
): List<Pair<LocalDateTime, LocalDateTime>> {
    val sessionStart = sessionEntity.actualStartTime ?: sessionEntity.createdAt
    val sessionEnd = minOf(
        sessionEntity.actualEndTime ?: sessionEntity.endTime,
        sessionEntity.endTime,
        currentDateTime
    )
    if (sessionEnd <= sessionStart) return emptyList()

    val intervals = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()
    var activeStart: LocalDateTime? = sessionStart
    val events = sessionEvents
        .filter { it.eventTime in sessionStart..sessionEnd }
        .sortedBy { it.eventTime }

    for ((_, _, eventTime, eventType) in events) {
        when (eventType) {
            SessionEvents.PAUSE_SESSION, SessionEvents.COMPLETE_SESSION -> {
                activeStart?.let { intervals.add(it to eventTime) }
                activeStart = null
                if (eventType == SessionEvents.COMPLETE_SESSION) break
            }

            SessionEvents.RESUME_SESSION -> {
                if (activeStart == null) activeStart = eventTime
            }

            else -> Unit
        }
    }

    activeStart?.let { intervals.add(it to sessionEnd) }
    return intervals
}
