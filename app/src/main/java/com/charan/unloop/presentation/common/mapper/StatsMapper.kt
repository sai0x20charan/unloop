package com.charan.unloop.presentation.common.mapper

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.model.SessionWithSessionEvents
import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.presentation.stats.FocusChartDataUiModel
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


internal fun SessionWithSessionEvents.toPauseIntervals(
    currentDateTime: LocalDateTime = DateTimeUtils.getCurrentDateTime()
): List<Pair<LocalDateTime, LocalDateTime>> {
    val sessionStart = sessionEntity.actualStartTime ?: sessionEntity.createdAt
    val sessionEnd = minOf(
        sessionEntity.actualEndTime ?: sessionEntity.scheduledEndTime,
        sessionEntity.scheduledEndTime,
        currentDateTime
    )

    if (sessionEnd <= sessionStart) return emptyList()

    val intervals = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()
    var pauseStart: LocalDateTime? = null

    val events = sessionEvents
        .filter { it.eventTime in sessionStart..sessionEnd }
        .sortedBy { it.eventTime }

    for ((_, _, eventTime, eventType) in events) {
        when (eventType) {
            SessionEventType.PAUSE_SESSION -> {
                if (pauseStart == null) pauseStart = eventTime
            }

            SessionEventType.RESUME_SESSION,
            SessionEventType.COMPLETE_SESSION -> {
                pauseStart?.let {
                    intervals.add(it to eventTime)
                }
                pauseStart = null

                if (eventType == SessionEventType.COMPLETE_SESSION) break
            }

            else -> Unit
        }
    }

    pauseStart?.let {
        intervals.add(it to sessionEnd)
    }

    return intervals
}

internal fun SessionWithSessionEvents.toFocusIntervals(
    currentDateTime: LocalDateTime = DateTimeUtils.getCurrentDateTime()
): List<Pair<LocalDateTime, LocalDateTime>> {
    val sessionStart = sessionEntity.actualStartTime ?: sessionEntity.createdAt
    val sessionEnd = minOf(
        sessionEntity.actualEndTime ?: sessionEntity.scheduledEndTime,
        sessionEntity.scheduledEndTime,
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
            SessionEventType.PAUSE_SESSION, SessionEventType.COMPLETE_SESSION -> {
                activeStart?.let { intervals.add(it to eventTime) }
                activeStart = null
                if (eventType == SessionEventType.COMPLETE_SESSION) break
            }

            SessionEventType.RESUME_SESSION -> {
                if (activeStart == null) activeStart = eventTime
            }

            else -> Unit
        }
    }

    activeStart?.let { intervals.add(it to sessionEnd) }
    return intervals
}
