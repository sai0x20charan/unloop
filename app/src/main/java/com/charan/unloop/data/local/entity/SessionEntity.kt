package com.charan.unloop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.presentation.routine.UnlockChallengeType
import kotlinx.datetime.LocalTime

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id : String,
    val routineId : String? = null,
    val actualStartTime : LocalTime? = null,
    val actualEndTime : LocalTime? = null,
    val startTime : LocalTime,
    val endTime : LocalTime,
    val sessionStatus : SessionStatus,
    val sessionType : SessionType,
    val blockedApps : List<String>? = null,
    val unlockChallengeType: UnlockChallengeType,
    val sessionName : String,
    val waitDurationSeconds : Int? = null
)
