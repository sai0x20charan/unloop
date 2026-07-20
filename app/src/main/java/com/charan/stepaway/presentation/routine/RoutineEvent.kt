package com.charan.stepaway.presentation.routine

import kotlinx.datetime.DayOfWeek

sealed class RoutineEvent {
    data class OnRoutineNameChange(val name: String) : RoutineEvent()
    data class OnRoutineDaySelect(val day: DayOfWeek) : RoutineEvent()
    data object OnAllDayToggle : RoutineEvent()
    data object OnStartTimeClick : RoutineEvent()
    data object OnEndTimeClick : RoutineEvent()
    data class OnRepeatIntervalChange(val interval: Int) : RoutineEvent()

    data object OnIntervalBlockingToggle : RoutineEvent()
    data class OnIntervalHoursChange(val hours: Int) : RoutineEvent()
    data class OnIntervalMinutesChange(val minutes: Int) : RoutineEvent()
    data class OnLockDurationHoursChange(val hours: Int) : RoutineEvent()
    data class OnLockDurationMinutesChange(val minutes: Int) : RoutineEvent()
    data class OnStartTimeChange(val hour: Int, val minute: Int) : RoutineEvent()
    data class OnEndTimeChange(val hour: Int, val minute: Int) : RoutineEvent()
    data object OnStartTimeDialogDismiss : RoutineEvent()
    data object OnEndTimeDialogDismiss : RoutineEvent()
    data object OnNextClick : RoutineEvent()
    data object OnBackClick : RoutineEvent()
    data object OnCreateRoutineClick : RoutineEvent()

    data class OnInstalledAppSelect(val packageName: String) : RoutineEvent()

    data class OnUnlockChallengeTypeChange(val type: UnlockChallengeType) : RoutineEvent()
    data class OnWaitTimeSecondsChange(val seconds: Int) : RoutineEvent()
    data class OnOneTimeUnlockWaitTimeChange(val hour: Int, val minute: Int) : RoutineEvent()
    data object OnOneTimeUnlockTimeClick : RoutineEvent()
    data object OnOneTimeUnlockTimeDialogDismiss : RoutineEvent()
}