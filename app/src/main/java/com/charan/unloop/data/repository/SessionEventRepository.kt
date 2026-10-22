package com.charan.unloop.data.repository

import com.charan.unloop.data.local.entity.SessionEventEntity
import com.charan.unloop.data.local.model.enums.SessionEvents

interface SessionEventRepository {
    suspend fun insertSessionEvent(sessionEvents: SessionEventEntity)

    suspend fun recordSessionEvent(sessionId : String, sessionEvents: SessionEvents,appPackage : String?)


}
