package com.charan.unloop.presentation.common.mapper

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.generateUuid
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.presentation.common.models.RoutineUiModel
import com.charan.unloop.presentation.home.CurrentSessionUiModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

fun RoutineUiModel.toRoutineEntity(id : String? = null, createdAt : LocalDateTime? = null) : RoutineEntity {
    return RoutineEntity(
        id = id ?: this.id.takeIf { it.isNotBlank() } ?: generateUuid(),
        name = name,
        description = description,
        startTime = startTime,
        endTime = endTime,
        daysOfWeek = selectedDays,

        isEnabled = isEnabled,
        isIntervalBlocking = false,
        unlockChallengeType = unlockChallengeType,
        waitDurationSeconds = waitDurationSeconds,
        areSessionControlsLocked = areSessionControlsLocked,
        createdAt = createdAt ?: DateTimeUtils.getCurrentDateTime(),
        updatedAt = DateTimeUtils.getCurrentDateTime(),
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
        selectedDays = daysOfWeek,
        startTime = startTime,
        endTime = endTime,
        blockedApps = disableAppPackages ?: emptyList(),
        isEnabled = isEnabled,
        unlockChallengeType = unlockChallengeType,
        waitDurationSeconds = waitDurationSeconds,
        areSessionControlsLocked = areSessionControlsLocked
    )
}

fun SessionEntity.toCurrentSessionUiModel() : CurrentSessionUiModel {
    val remaining = if (sessionStatus == SessionStatus.PAUSED && pausedRemainingSeconds != null) {
        pausedRemainingSeconds.toInt()
    } else {
        DateTimeUtils.getRemainingDuration(endTime).toInt()
    }

    val total = (endTime.toInstant(TimeZone.currentSystemDefault()) -
        startTime.toInstant(TimeZone.currentSystemDefault())).inWholeSeconds.toInt().coerceAtLeast(1)

    return CurrentSessionUiModel(
        sessionId = id,
        routineId = routineId,
        sessionName = sessionName,
        sessionStatus = sessionStatus,
        endTime = endTime.toDisplayString(),
        remainingSeconds = remaining,
        totalSeconds = total,
        blockedPackages = blockedApps.orEmpty(),
        areSessionControlsLocked = areSessionControlsLocked
    )
}

