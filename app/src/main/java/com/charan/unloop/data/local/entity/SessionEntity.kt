package com.charan.unloop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(tableName = "sessions")
@Serializable
data class SessionEntity(
    @PrimaryKey
    val id : String,
    val routineId : String? = null,
    val actualStartTime : LocalDateTime? = null,
    val actualEndTime : LocalDateTime? = null,
    val scheduledStartTime : LocalDateTime,
    val scheduledEndTime : LocalDateTime,
    val status : SessionStatus,
    val type : SessionType,
    val blockedAppPackages : List<String>? = null,
    val unlockChallengeType: UnlockChallengeType,
    val name : String,
    val waitDurationSeconds : Int? = null,
    val createdAt : LocalDateTime,
    val pausedRemainingSeconds : Long? = null,
    val areSessionControlsLocked: Boolean = false,
)
