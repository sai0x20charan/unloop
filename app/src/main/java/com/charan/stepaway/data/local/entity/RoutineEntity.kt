package com.charan.stepaway.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charan.stepaway.data.local.model.enums.ScheduleType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.uuid.Uuid

@Entity(tableName = "routines")
data class RoutineEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val description: String?,

    val scheduleType: ScheduleType,

    val startTime: LocalTime?,

    val endTime: LocalTime?,

    val daysOfWeek: List<DayOfWeek>?,

    val frequencyMinutes: Int?,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime,

    val isEnabled: Boolean = true,

    val isDeleted: Boolean = false,

    val isIntervalBlocking: Boolean = false,

    val intervalHours: Int? = null,

    val intervalMinutes: Int? = null,

    val lockDurationHours: Int? = null,

    val lockDurationMinutes: Int? = null
)