package com.charan.unloop.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.entity.SessionEventEntity
import kotlinx.serialization.Serializable

@Serializable
data class SessionWithSessionEvents(
    @Embedded val sessionEntity: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    ) val sessionEvents: List<SessionEventEntity>
)