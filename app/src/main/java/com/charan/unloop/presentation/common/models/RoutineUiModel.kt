package com.charan.unloop.presentation.common.models

import com.charan.unloop.presentation.routine.UnlockChallengeType
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class RoutineUiModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val selectedDays: List<DayOfWeek> = emptyList(),
    val startTime: LocalTime = LocalTime(0, 0),
    val endTime: LocalTime = LocalTime(0, 0),
    val blockedApps: List<String> = emptyList(),
    val isEnabled: Boolean = true,

    val unlockChallengeType: UnlockChallengeType = UnlockChallengeType.WAIT_TIME,
    val waitDurationSeconds: Int? = null,
    val areSessionControlsLocked: Boolean = false,
) {
    val formattedTime: String
        get() = when {
            startTime != null && endTime != null -> "${startTime.toDisplayString()} - ${endTime.toDisplayString()}"
            else -> ""
        }

    val formattedDays: String
        get() = when {
            selectedDays.isEmpty() -> "No days set"
            selectedDays.size == 7 -> "Every day"
            selectedDays.size == 5 && selectedDays.containsAll(
                listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            ) -> "Weekdays"
            selectedDays.size == 2 && selectedDays.containsAll(
                listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            ) -> "Weekends"
            else -> selectedDays.joinToString(", ") {
                it.name.lowercase().replaceFirstChar { char -> char.titlecase() }.take(3)
            }
        }
}
