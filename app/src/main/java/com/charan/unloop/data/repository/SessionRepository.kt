package com.charan.unloop.data.repository

import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.SessionWithSessionEvents
import com.charan.unloop.data.local.model.enums.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.LocalDateTime

interface SessionRepository {

    /** Shared Room state. Replays the latest running or paused session after loading. */
    val activeSession: SharedFlow<SessionEntity?>

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

    suspend fun getAllSessions() : List<SessionEntity>

    suspend fun getSessionWithSessionEventForRange(
        startDate: LocalDateTime,
        endTime: LocalDateTime
    ): List<SessionWithSessionEvents>

    suspend fun getSessionWithSessionEventById(sessionId: String): SessionWithSessionEvents

}
