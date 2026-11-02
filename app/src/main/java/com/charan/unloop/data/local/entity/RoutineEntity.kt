package com.charan.unloop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charan.unloop.data.local.model.enums.ScheduleType
import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val description: String?,

    val isAllDay : Boolean ,

    val startTime: LocalTime,

    val endTime: LocalTime,

    val daysOfWeek: List<DayOfWeek>,

    val unlockChallengeType: UnlockChallengeType,

    val waitDurationSeconds: Int?,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime,

    val isEnabled: Boolean = true,

    val isDeleted: Boolean = false,

    val isIntervalBlocking: Boolean = false,
    val intervalMinutes : Int?,
    val blockedAppPackages : List<String>?,

    val areSessionControlsLocked: Boolean = false,

    )
