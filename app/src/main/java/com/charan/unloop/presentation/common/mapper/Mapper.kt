package com.charan.unloop.presentation.common.mapper

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.getHour
import com.charan.unloop.core.utils.DateTimeUtils.getMinute
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.generateUuid
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.presentation.common.model.AppInfoUiModel
import com.charan.unloop.presentation.common.model.RoutineUiModel
import com.charan.unloop.presentation.home.CurrentSessionUiModel
import com.charan.unloop.presentation.home.InstantSessionUiModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration

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
        blockedAppPackages = blockedAppPackages,
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
        blockedAppPackages = blockedAppPackages ?: emptyList(),
        isEnabled = isEnabled,
        unlockChallengeType = unlockChallengeType,
        waitDurationSeconds = waitDurationSeconds,
        areSessionControlsLocked = areSessionControlsLocked
    )
}

fun SessionEntity.toCurrentSessionUiModel() : CurrentSessionUiModel {
    val remaining = if (status == SessionStatus.PAUSED && pausedRemainingSeconds != null) {
        pausedRemainingSeconds.toInt()
    } else {
        DateTimeUtils.getRemainingSeconds(scheduledEndTime).toInt()
    }

    val total = (scheduledEndTime.toInstant(TimeZone.currentSystemDefault()) -
        scheduledStartTime.toInstant(TimeZone.currentSystemDefault())).inWholeSeconds.toInt().coerceAtLeast(1)

    return CurrentSessionUiModel(
        sessionId = id,
        routineId = routineId,
        sessionName = name,
        sessionStatus = status,
        endTime = scheduledEndTime.toDisplayString(),
        remainingSeconds = remaining,
        totalSeconds = total,
        blockedAppPackages = blockedAppPackages.orEmpty(),
        areSessionControlsLocked = areSessionControlsLocked
    )
}

fun SessionEntity.toInstantSessionUiModel(
    installedApps: List<AppInfoUiModel>
): InstantSessionUiModel {
    val duration = DateTimeUtils.getDuration(createdAt, scheduledEndTime).coerceAtLeast(Duration.ZERO)
    val blockedAppPackageSet = blockedAppPackages.orEmpty().toSet()
    return InstantSessionUiModel(
        sessionTitle = name,
        installedApps = installedApps.map { app ->
            app.copy(isSelected = app.packageName in blockedAppPackageSet)
        },
        areSessionControlsLocked = areSessionControlsLocked,
        unlockChallengeType = unlockChallengeType,
        waitDurationSeconds = waitDurationSeconds ?: 30,
        hour = duration.getHour(),
        minute = duration.getMinute()
    )
}
