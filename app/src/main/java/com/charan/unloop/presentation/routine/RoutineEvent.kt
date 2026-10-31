package com.charan.unloop.presentation.routine

import kotlinx.datetime.DayOfWeek

sealed class RoutineEvent {
    data class OnRoutineNameChange(val name: String) : RoutineEvent()
    data class OnRoutineDaySelect(val day: DayOfWeek) : RoutineEvent()
    data object OnStartTimeToggle : RoutineEvent()
    data object OnEndTimeToggle : RoutineEvent()

    data class OnStartTimeChange(val hour: Int, val minute: Int) : RoutineEvent()
    data class OnEndTimeChange(val hour: Int, val minute: Int) : RoutineEvent()
    data object OnCreateRoutineClick : RoutineEvent()

    data class OnInstalledAppSelect(val packageName: String) : RoutineEvent()
    data object OnSelectAllAppsClick : RoutineEvent()

    data class OnUnlockChallengeTypeChange(val type: UnlockChallengeType) : RoutineEvent()
    data class OnWaitTimeSecondsChange(val seconds: Int) : RoutineEvent()
    data class OnSessionControlsLockedChange(val locked: Boolean) : RoutineEvent()

    data object OnScheduleToggle : RoutineEvent()
    data object OnAppsSheetToggle : RoutineEvent()
}
