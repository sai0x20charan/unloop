package com.charan.unloop.data.repository.impl

import com.charan.unloop.data.local.dao.SessionDao
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {
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
        sessionDao.getActiveSession()

    override suspend fun getActiveSessionOnce(): SessionEntity? =
        sessionDao.getActiveSessionOnce()

    override suspend fun getSessionById(sessionId: String): SessionEntity? =
        sessionDao.getSessionById(sessionId)

    override suspend fun getLastInstantSession(): SessionEntity? =
        sessionDao.getLastInstantSession()
}
