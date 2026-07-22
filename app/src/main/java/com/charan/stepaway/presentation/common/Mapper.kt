package com.charan.stepaway.presentation.common

import com.charan.stepaway.core.utils.DateUtils
import com.charan.stepaway.data.local.entity.RoutineEntity
import com.charan.stepaway.presentation.routine.RoutineState
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