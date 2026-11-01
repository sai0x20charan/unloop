package com.charan.unloop.data.repository.impl

import com.charan.unloop.core.di.ApplicationScope
import com.charan.unloop.data.local.dao.SessionDao
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.SessionWithSessionEvents
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    @ApplicationScope applicationScope: CoroutineScope
) : SessionRepository {
    override val activeSession = sessionDao.getActiveSession()
        .distinctUntilChanged()
        .shareIn(applicationScope, SharingStarted.Lazily, replay = 1)

    override suspend fun insertSession(session: SessionEntity) =
        sessionDao.insertSession(session)

    override suspend fun updateSession(session: SessionEntity) =
        sessionDao.updateSession(session)

    override suspend fun updateSessionStatus(
        sessionId: String,
        status: SessionStatus,
        actualEndTime: LocalDateTime?
    ) = sessionDao.updateSessionStatus(sessionId, status, actualEndTime)

    override fun getActiveSession(): Flow<SessionEntity?> =
        activeSession

    override suspend fun getActiveSessionOnce(): SessionEntity? =
        sessionDao.getActiveSessionOnce()

    override suspend fun getSessionById(sessionId: String): SessionEntity? =
        sessionDao.getSessionById(sessionId)

    override suspend fun getLastInstantSession(): SessionEntity? =
        sessionDao.getLastInstantSession()

    override suspend fun getAllSessions(): List<SessionEntity> {
        return sessionDao.getAllSessions()
    }

    override suspend fun getSessionWithSessionEventForRange(
        startDate: LocalDateTime,
        endTime: LocalDateTime
    ): List<SessionWithSessionEvents>{
        return sessionDao.getSessionForRange(startDate, endTime)
    }
}
