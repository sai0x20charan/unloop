package com.charan.unloop.presentation.routine_details

import com.charan.unloop.presentation.common.model.AppInfoUiModel
import com.charan.unloop.presentation.common.model.RoutineUiModel
import kotlinx.datetime.LocalTime

data class RoutineDetailsState(
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


