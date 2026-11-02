package com.charan.unloop.presentation.common.model

import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import com.charan.unloop.core.utils.DateTimeUtils
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.time.Duration

data class RoutineUiModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val selectedDays: List<DayOfWeek> = emptyList(),
    val startTime: LocalTime = LocalTime(0, 0),
    val endTime: LocalTime = LocalTime(0, 0),
    val blockedAppPackages: List<String> = emptyList(),
    val isEnabled: Boolean = true,

    val unlockChallengeType: UnlockChallengeType = UnlockChallengeType.STRICT_MODE,
    val waitDurationSeconds: Int? = null,
    val areSessionControlsLocked: Boolean = false,
) {
    val duration: Duration
        get() = DateTimeUtils.getRoutineDuration(startTime, endTime)

}
