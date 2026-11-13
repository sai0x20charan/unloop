package com.charan.unloop.data.repository

import com.charan.unloop.data.local.entity.SessionEventEntity
import com.charan.unloop.data.local.model.enums.SessionEventType

interface SessionEventRepository {
    suspend fun insertSessionEvent(sessionEvents: SessionEventEntity)

    suspend fun insertSessionEvents(sessionEvents: List<SessionEventEntity>)

    suspend fun deleteEventsForSessions(sessionIds: List<String>)

    suspend fun recordSessionEvent(sessionId : String, sessionEvents: SessionEventType,packageName : String?)

    suspend fun getEventsForSession(sessionId : String) : List<SessionEventEntity>


}
