package com.charan.unloop.presentation.common

import com.charan.unloop.core.utils.DateUtils
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.presentation.common.models.RoutineUiModel
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun RoutineUiModel.toRoutineEntity(id : String? = null, createdAt : LocalDateTime? = null) : RoutineEntity {
    return RoutineEntity(
        id = id ?: this.id.takeIf { it.isNotBlank() } ?: Uuid.generateV4().toString(),
        name = name,
        description = description,
        startTime = startTime,
        endTime = endTime,
        daysOfWeek = selectedDays,

        isEnabled = isEnabled,
        isIntervalBlocking = false,
        unlockChallengeType = unlockChallengeType,
        waitDurationSeconds = waitDurationSeconds,
        createdAt = createdAt ?: DateUtils.currentDateTime(),
        updatedAt = DateUtils.currentDateTime(),
        disableAppPackages = blockedApps,
        isAllDay = false,
        isDeleted = false,
        intervalMinutes = 0
    )
}

fun RoutineEntity.toRoutineUiModel(): RoutineUiModel {
    return RoutineUiModel(
        id = id,
        name = name,
        description = description ?: "",
        selectedDays = daysOfWeek ?: emptyList(),
        startTime = startTime,
        endTime = endTime,
        blockedApps = disableAppPackages ?: emptyList(),
        isEnabled = isEnabled,
        unlockChallengeType = unlockChallengeType,
        waitDurationSeconds = waitDurationSeconds
    )
}
