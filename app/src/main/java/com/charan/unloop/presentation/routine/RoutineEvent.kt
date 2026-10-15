package com.charan.unloop.presentation.routine

import kotlinx.datetime.DayOfWeek

sealed class RoutineEvent {
    data class OnRoutineNameChange(val name: String) : RoutineEvent()
    data class OnRoutineDaySelect(val day: DayOfWeek) : RoutineEvent()
    data object OnAllDayToggle : RoutineEvent()
    data object OnStartTimeToggle : RoutineEvent()
    data object OnEndTimeToggle : RoutineEvent()
    data object OnIntervalTimeToggle : RoutineEvent()
    data class OnRepeatIntervalChange(val interval: Int) : RoutineEvent()

    data object OnIntervalBlockingToggle : RoutineEvent()
    data class OnIntervalTimeChange(val hours: Int, val minutes: Int) : RoutineEvent()
    data class OnStartTimeChange(val hour: Int, val minute: Int) : RoutineEvent()
    data class OnEndTimeChange(val hour: Int, val minute: Int) : RoutineEvent()
    data object OnNextClick : RoutineEvent()
    data object OnBackClick : RoutineEvent()
    data object OnCreateRoutineClick : RoutineEvent()

    data class OnInstalledAppSelect(val packageName: String) : RoutineEvent()

    data class OnUnlockChallengeTypeChange(val type: UnlockChallengeType) : RoutineEvent()
    data class OnWaitTimeSecondsChange(val seconds: Int) : RoutineEvent()
}