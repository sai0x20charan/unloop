package com.charan.unloop.data.repository.impl

import android.text.format.DateUtils
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.getCurrentTime
import com.charan.unloop.data.local.dao.SessionEventDao
import com.charan.unloop.data.local.entity.SessionEventEntity
import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.data.repository.SessionEventRepository
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SessionEventRepositoryImpl @Inject constructor(
    private val sessionEventDao: SessionEventDao
) : SessionEventRepository {
    override suspend fun insertSessionEvent(sessionEvents: SessionEventEntity) {
        sessionEventDao.insertSessionEvent(sessionEvents)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun recordSessionEvent(
        sessionId: String,
        sessionEvents: SessionEvents,
        appPackage: String?
    ) {

        val sessionEventEntity = SessionEventEntity(
            id = Uuid.generateV4().toString(),
            sessionId = sessionId,
            eventTime = DateTimeUtils.getCurrentDateTime(),
            eventType = sessionEvents,
            packageName = appPackage
        )

        insertSessionEvent(sessionEventEntity)
    }
}