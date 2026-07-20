package com.charan.stepaway.presentation.routine

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
    val showOneTimeUnlockTimeDialog : Boolean = false,
    val installedApps : List<InstalledAppsUiModel> = emptyList(),
    val stepTitles: List<String> = listOf("Schedule", "Select Apps", "Unlock Challenge", "Review & Confirm"),
    val unlockChallengeType: UnlockChallengeType = UnlockChallengeType.WAIT_TIME,
    val waitTimeSeconds: Int = 30,
    val oneTimeUnlockWaitTimeHours: Int = 0,
    val oneTimeUnlockWaitTimeMinutes: Int = 5
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
    val lockDurationHours : Int = 0,
    val lockDurationMinutes : Int = 10
)

data class InstalledAppsUiModel(
    val appName : String = "",
    val packageName : String = "",
    val icon : Drawable? = null,
    val isSelected : Boolean = false
)