package com.charan.unloop.presentation.common.mapper

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.presentation.common.model.FocusSegmentUiModel
import kotlin.time.Duration
import kotlinx.datetime.LocalDateTime

fun List<Pair<LocalDateTime, LocalDateTime>>.toFocusSegmentUiModels(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    actualEndTime: LocalDateTime? = null,
): List<FocusSegmentUiModel> {
    val totalDuration = DateTimeUtils.getDuration(startTime, endTime)
    if (totalDuration <= Duration.ZERO) return emptyList()

    val sessionEnd = minOf(maxOf(actualEndTime ?: endTime, startTime), endTime)
    val sessionEndFraction = (DateTimeUtils.getDuration(startTime, sessionEnd) / totalDuration)
        .toFloat()
        .coerceIn(0f, 1f)

    val focusSegments = mapNotNull { (start, end) ->
        val intervalStart = maxOf(start, startTime)
        val intervalEnd = minOf(end, sessionEnd)
        if (intervalStart >= intervalEnd) return@mapNotNull null

        val startFraction = (DateTimeUtils.getDuration(startTime, intervalStart) / totalDuration)
            .toFloat()
            .coerceIn(0f, 1f)
        val endFraction = (DateTimeUtils.getDuration(startTime, intervalEnd) / totalDuration)
            .toFloat()
            .coerceIn(0f, 1f)
        if (startFraction >= endFraction) return@mapNotNull null

        FocusSegmentUiModel(
            startFraction = startFraction,
            endFraction = endFraction,
        )
    }

    return buildList {
        var previousEndFraction = 0f

        focusSegments.sortedBy { it.startFraction }.forEach { segment ->
            if (segment.startFraction > previousEndFraction) {
                add(
                    FocusSegmentUiModel(
                        startFraction = previousEndFraction,
                        endFraction = segment.startFraction,
                        isFocused = false,
                    )
                )
            }
            add(segment)
            previousEndFraction = maxOf(previousEndFraction, segment.endFraction)
        }

        if (previousEndFraction < sessionEndFraction) {
            add(
                FocusSegmentUiModel(
                    startFraction = previousEndFraction,
                    endFraction = sessionEndFraction,
                    isFocused = false,
                )
            )
        }

        if (sessionEndFraction < 1f) {
            add(
                FocusSegmentUiModel(
                    startFraction = sessionEndFraction,
                    endFraction = 1f,
                    isFocused = false,
                    isEnded = true,
                )
            )
        }
    }
}
