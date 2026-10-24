package com.charan.unloop.presentation.home

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.presentation.common.models.InstalledAppsUiModel

data class HomeState(
    val instantSessionUiModel: InstantSessionUiModel = InstantSessionUiModel(),
    val currentSessionUiModel: CurrentSessionUiModel = CurrentSessionUiModel(),
    val hasExactAlarmPermission: Boolean = true,
)

data class InstantSessionUiModel(
    val installedApps: List<InstalledAppsUiModel> = emptyList(),
    val showBottomSheet: Boolean = false,
    val sessionTitle: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val isHourValid: Boolean = true,
    val isMinutesValid: Boolean = true,
    val durationPresets: List<Int> = listOf(15, 30, 45, 60, 120),
    val isSessionTimeEditing: Boolean = false
) {
    val totalSeconds: Int
        get() = if (isHourValid && isMinutesValid) hour * 3600 + minute * 60 else 0

    val durationFormatted: String
        get() {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            return String.format("%02d:%02d", hours, minutes)
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
    val sessionStatus : SessionStatus = SessionStatus.STOPPED


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
