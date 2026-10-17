package com.charan.unloop.presentation.routine

import android.graphics.drawable.Drawable
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
    val stepTitles: List<String> = listOf("Schedule", "Select Apps", "Unlock Challenge", "Review & Confirm"),
)


