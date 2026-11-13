package com.charan.unloop.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.charan.unloop.data.local.model.enums.SessionEventType
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Entity(tableName = "session_events")
@Serializable
data class SessionEventEntity(
    @PrimaryKey
    val id : String,
    val sessionId : String,
    val eventTime : LocalDateTime,
    val eventType : SessionEventType,
    val packageName : String? =  null
)