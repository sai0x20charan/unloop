package com.charan.unloop.presentation.routine_details

import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import kotlinx.datetime.DayOfWeek

sealed class RoutineDetailsEvent {
    data class OnRoutineNameChange(val name: String) : RoutineDetailsEvent()
    data class OnRoutineDaySelect(val day: DayOfWeek) : RoutineDetailsEvent()
    data object OnStartTimeToggle : RoutineDetailsEvent()
    data object OnEndTimeToggle : RoutineDetailsEvent()

    data class OnStartTimeChange(val hour: Int, val minute: Int) : RoutineDetailsEvent()
    data class OnEndTimeChange(val hour: Int, val minute: Int) : RoutineDetailsEvent()
    data object OnSaveRoutineClick : RoutineDetailsEvent()

    data class OnInstalledAppSelect(val packageName: String) : RoutineDetailsEvent()
    data object OnSelectAllAppsClick : RoutineDetailsEvent()

    data class OnUnlockChallengeTypeChange(val type: UnlockChallengeType) : RoutineDetailsEvent()
    data class OnWaitTimeSecondsChange(val seconds: Int) : RoutineDetailsEvent()
    data class OnSessionControlsLockedChange(val locked: Boolean) : RoutineDetailsEvent()

    data object OnScheduleToggle : RoutineDetailsEvent()
    data object OnAppsSheetToggle : RoutineDetailsEvent()
}
