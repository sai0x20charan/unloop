package com.charan.unloop.presentation.home

import com.charan.unloop.presentation.common.models.InstalledAppsUiModel

data class HomeState(
    val instantSessionUiModel: InstantSessionUiModel = InstantSessionUiModel(),
    val startSessionState: StartSessionState = StartSessionState(),
)

data class InstantSessionUiModel(
    val installedApps: List<InstalledAppsUiModel> = emptyList(),
    val showBottomSheet: Boolean = false,
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
}

data class StartSessionState(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isPaused: Boolean = false,
) {
    val remainingTimeFormatted: String
        get() {
            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}
