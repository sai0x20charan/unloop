package com.charan.unloop.presentation.home

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.presentation.common.models.AppInfoUiModel
import com.charan.unloop.presentation.routine.UnlockChallengeType

data class HomeState(
    val instantSessionUiModel: InstantSessionUiModel = InstantSessionUiModel(),
    val currentSessionUiModel: CurrentSessionUiModel = CurrentSessionUiModel(),
)

data class InstantSessionUiModel(
    val isTitleEditable : Boolean = false,
    val installedApps: List<AppInfoUiModel> = emptyList(),
    val showBottomSheet: Boolean = false,
    val sessionTitle: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val isHourValid: Boolean = true,
    val isMinutesValid: Boolean = true,
    val unlockChallengeType: UnlockChallengeType = UnlockChallengeType.STRICT_MODE,
    val durationPresets: List<Int> = listOf(15, 30, 45, 60, 120),
    val isSessionTimeEditing: Boolean = false,
    val areSessionControlsLocked : Boolean = false,
    val waitTime : Int = 30
) {
    val totalSeconds: Int
        get() = if (isHourValid && isMinutesValid) hour * 3600 + minute * 60 else 0

    val durationFormatted: String
        get() {
            return DateTimeUtils.formatHoursAndMinutes(totalSeconds)
        }

    val hasSelectedApps: Boolean
        get() = installedApps.any { it.isSelected }

    val canStart: Boolean
        get() = totalSeconds > 0 && hasSelectedApps
}

data class CurrentSessionUiModel(
    val sessionId : String = "",
    val routineId : String? = null,
    val sessionName: String = "",
    val endTime: String = "",
    val blockedPackages: List<String> = emptyList(),
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val sessionStatus : SessionStatus = SessionStatus.STOPPED,
    val areSessionControlsLocked: Boolean = false


) {

    val progress : Float
        get() = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds else 0f
    val remainingTimeFormatted: String
        get() {
            return DateTimeUtils.formatDuration(remainingSeconds)
        }
    val isRunning: Boolean
        get() = sessionStatus == SessionStatus.RUNNING

    val isPaused: Boolean
        get() = sessionStatus == SessionStatus.PAUSED
}
