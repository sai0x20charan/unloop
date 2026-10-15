package com.charan.unloop.presentation.routine

import android.graphics.drawable.Drawable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

enum class UnlockChallengeType {
    WAIT_TIME,
    ONE_TIME_UNLOCK,
    STRICT_MODE
}

data class RoutineState(
    val pageState : Int = 0,
    val totalPageState : Int = 4,
    val routineName : String = "",
    val routineDescription : String = "",
    val routineScheduleState : RoutineScheduleState = RoutineScheduleState(),
    val showEndTimeDialog : Boolean = false,
    val showStartTimeDialog : Boolean = false,
    val showIntervalDialog : Boolean = false,
    val installedApps : List<InstalledAppsUiModel> = emptyList(),
    val stepTitles: List<String> = listOf("Schedule", "Select Apps", "Unlock Challenge", "Review & Confirm"),
)

data class RoutineScheduleState(
    val routineStartTime : LocalTime = LocalTime(0, 0, 0),
    val routineEndTime : LocalTime = LocalTime(0, 0, 0),

    val routineDays : List<DayOfWeek> = emptyList(),
    val routineFrequency : Int = 0,
    val isAllDay : Boolean = false,
    val isIntervalBlocking : Boolean = false,
    val intervalHours : Int = 1,
    val intervalMinutes : Int = 0,
    val unlockChallengeType: UnlockChallengeType = UnlockChallengeType.WAIT_TIME,
    val waitTimeSeconds: Int = 30,
)

data class InstalledAppsUiModel(
    val appName : String = "",
    val packageName : String = "",
    val icon : Drawable? = null,
    val isSelected : Boolean = false
)