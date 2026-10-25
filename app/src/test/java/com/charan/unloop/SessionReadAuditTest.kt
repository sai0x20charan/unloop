package com.charan.unloop

import com.charan.unloop.data.local.dao.SessionDao
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.impl.SessionRepositoryImpl
import com.charan.unloop.presentation.routine.UnlockChallengeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionReadAuditTest {
    @Test fun readsAfterWritesMustReturnTheNewSession() = runBlocking {
        val time = LocalDateTime(2026, 10, 25, 12, 0)
        val initial = SessionEntity(
            id = "audit", startTime = time, endTime = time,
            sessionStatus = SessionStatus.PAUSED, sessionType = SessionType.INSTANT,
            unlockChallengeType = UnlockChallengeType.STRICT_MODE,
            sessionName = "Audit", createdAt = time, pausedRemainingSeconds = 60
        )
        val dao = AuditDao(initial)
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val repo = SessionRepositoryImpl(dao, scope)
            assertEquals(initial, repo.getActiveSessionOnce())
            val resumed = initial.copy(sessionStatus = SessionStatus.RUNNING,
                endTime = LocalDateTime(2026, 10, 25, 12, 1), pausedRemainingSeconds = null)
            repo.updateSession(resumed)
            assertEquals(resumed, repo.getSessionById("audit"))
        } finally { scope.cancel() }
    }

    private class AuditDao(var stored: SessionEntity) : SessionDao {
        val changes = MutableStateFlow<SessionEntity?>(stored)
        override suspend fun insertSession(session: SessionEntity) = updateSession(session)
        override suspend fun updateSession(session: SessionEntity) {
            stored = session
            changes.value = session
        }
        override fun getActiveSession(statuses: List<SessionStatus>) = changes
        override suspend fun getActiveSessionOnce(statuses: List<SessionStatus>) = stored
        override suspend fun getSessionById(sessionId: String) = stored.takeIf { it.id == sessionId }
        override suspend fun getLastInstantSession(sessionType: SessionType) = stored
        override suspend fun updateSessionStatus(sessionId: String, status: SessionStatus,
            actualEndTime: LocalDateTime?) {
            updateSession(stored.copy(sessionStatus = status, actualEndTime = actualEndTime))
        }
    }
}
