package com.charan.unloop.presentation.routine

import androidx.annotation.StringRes
import com.charan.unloop.R
import com.charan.unloop.presentation.common.models.InstalledAppsUiModel
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
    val pageState : Int = 0,
    val totalPageState : Int = 4,
    val showEndTimeDialog : Boolean = false,
    val showStartTimeDialog : Boolean = false,
    val showIntervalDialog : Boolean = false,
    val installedApps : List<InstalledAppsUiModel> = emptyList(),
    @StringRes val stepTitles: List<Int> = listOf(
        R.string.routine_step_schedule,
        R.string.routine_step_select_apps,
        R.string.routine_focus_rules,
        R.string.routine_step_review
    ),
)


