package com.charan.unloop.presentation.common

import com.charan.unloop.core.utils.DateUtils
import com.charan.unloop.core.utils.DateUtils.toDisplayString
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.presentation.common.models.RoutineUiModel
import com.charan.unloop.presentation.routine.RoutineState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun RoutineState.toRoutineEntity(id : String? = null, createdAt : LocalDateTime? = null) : RoutineEntity {
    return RoutineEntity(
        id = id ?: Uuid.generateV4().toString(),
        name = this.routineName,
        description = this.routineDescription,
        startTime = this.routineScheduleState.routineStartTime,
        endTime = this.routineScheduleState.routineEndTime,
        daysOfWeek = this.routineScheduleState.routineDays,
        isAllDay = this.routineScheduleState.isAllDay,
        isIntervalBlocking = this.routineScheduleState.isIntervalBlocking,
        intervalMinutes = this.routineScheduleState.intervalHours * 60 + this.routineScheduleState.intervalMinutes,
        unlockChallengeType = this.routineScheduleState.unlockChallengeType,
        waitDurationSeconds = this.routineScheduleState.waitTimeSeconds,
        createdAt = createdAt ?: DateUtils.currentDateTime(),
        updatedAt = DateUtils.currentDateTime(),
        disableAppPackages = this.installedApps.filter { it.isSelected }.map { it.packageName }
    )
}

fun RoutineEntity.toRoutineUiModel(): RoutineUiModel {
    val formattedTime = if (isAllDay) {
        "All Day"
    } else if (startTime != null && endTime != null) {
        "${startTime.toDisplayString()} - ${endTime.toDisplayString()}"
    } else {
        ""
    }

    val formattedDays = when {
        daysOfWeek.isNullOrEmpty() -> "No days set"
        daysOfWeek.size == 7 -> "Every day"
        daysOfWeek.containsAll(listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) && daysOfWeek.size == 5 -> "Weekdays"
        daysOfWeek.containsAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) && daysOfWeek.size == 2 -> "Weekends"
        else -> daysOfWeek.joinToString(", ") { it.name.lowercase().replaceFirstChar { c -> c.titlecase() }.take(3) }
    }

    return RoutineUiModel(
        id = id,
        name = name,
        description = description ?: "",
        selectedDays = daysOfWeek ?: emptyList(),
        startTime = startTime?.toDisplayString() ?: "",
        endTime = endTime?.toDisplayString() ?: "",
        isAllDay = isAllDay,
        blockedApps = disableAppPackages ?: emptyList(),
        isEnabled = isEnabled,
        isIntervalBlocking = isIntervalBlocking,
        intervalMinutes = intervalMinutes,
        unlockChallengeType = unlockChallengeType,
        waitDurationSeconds = waitDurationSeconds,
        formattedTime = formattedTime,
        formattedDays = formattedDays
    )
}
