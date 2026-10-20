package com.charan.unloop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.presentation.routine.UnlockChallengeType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id : String,
    val routineId : String? = null,
    val actualStartTime : LocalDateTime? = null,
    val actualEndTime : LocalDateTime? = null,
    val startTime : LocalDateTime,
    val endTime : LocalDateTime,
    val sessionStatus : SessionStatus,
    val sessionType : SessionType,
    val blockedApps : List<String>? = null,
    val unlockChallengeType: UnlockChallengeType,
    val sessionName : String,
    val waitDurationSeconds : Int? = null,
    val createdAt : LocalDateTime,
    val pausedRemainingSeconds : Long? = null,
)
