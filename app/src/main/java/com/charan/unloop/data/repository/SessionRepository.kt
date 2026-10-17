package com.charan.unloop.data.repository

import com.charan.unloop.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    suspend fun insertSession(session: SessionEntity)

    fun getActiveSession(): Flow<SessionEntity?>

    suspend fun getSessionById(sessionId: String): SessionEntity?

}
