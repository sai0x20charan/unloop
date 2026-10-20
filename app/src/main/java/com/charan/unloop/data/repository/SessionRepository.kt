package com.charan.unloop.data.repository

import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface SessionRepository {

    suspend fun insertSession(session: SessionEntity)

    suspend fun updateSession(session: SessionEntity)

    suspend fun updateSessionStatus(
        sessionId: String,
        status: SessionStatus,
        actualEndTime: LocalDateTime? = null
    )

    fun getActiveSession(): Flow<SessionEntity?>

    suspend fun getActiveSessionOnce(): SessionEntity?

    suspend fun getSessionById(sessionId: String): SessionEntity?

    suspend fun getLastInstantSession() : SessionEntity?

}
