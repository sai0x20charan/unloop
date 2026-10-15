package com.charan.unloop.presentation.common.models

import com.charan.unloop.presentation.routine.UnlockChallengeType
import kotlinx.datetime.DayOfWeek

data class RoutineUiModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val selectedDays: List<DayOfWeek> = emptyList(),
    val startTime: String = "",
    val endTime: String = "",
    val isAllDay: Boolean = false,
    val blockedApps: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val isIntervalBlocking: Boolean = false,
    val intervalMinutes: Int? = null,
    val unlockChallengeType: UnlockChallengeType = UnlockChallengeType.WAIT_TIME,
    val waitDurationSeconds: Int? = null,
    val formattedTime: String = "",
    val formattedDays: String = "",
)
