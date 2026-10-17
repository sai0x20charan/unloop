package com.charan.unloop.data.repository.impl

import com.charan.unloop.data.local.dao.SessionDao
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao

) : SessionRepository {
    override suspend fun insertSession(session: SessionEntity) {
        withContext(Dispatchers.IO){
            sessionDao.insertSession(session)
        }
    }

    override fun getActiveSession(): Flow<SessionEntity?> {
        return sessionDao.getActiveSession()
    }

    override suspend fun getSessionById(sessionId: String): SessionEntity? {
        return withContext(Dispatchers.IO){
            sessionDao.getSessionById(sessionId)
        }
    }
}
