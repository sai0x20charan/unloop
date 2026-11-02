package com.charan.unloop.presentation.routine

import com.charan.unloop.presentation.common.models.AppInfoUiModel
import com.charan.unloop.presentation.common.models.RoutineUiModel
import kotlinx.datetime.LocalTime

enum class UnlockChallengeType {
    WAIT_TIME,
    STRICT_MODE
}

data class RoutineState(
    val routine: RoutineUiModel = RoutineUiModel(
        startTime = LocalTime(0, 0),
        endTime = LocalTime(0, 0),
        waitDurationSeconds = 30
    ),
    val showEndTimeDialog: Boolean = false,
    val showStartTimeDialog: Boolean = false,
    val showAppsSheet: Boolean = false,
    val installedApps: List<AppInfoUiModel> = emptyList(),
    val isScheduleExpanded: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
)


