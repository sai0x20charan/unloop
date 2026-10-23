package com.charan.unloop.data.repository.impl

import com.charan.unloop.core.overlay.RoutineSessionController
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.toLocalDateTime
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionManagerRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.routine.UnlockChallengeType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SessionManagerRepositoryImpl @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val routineSessionController: RoutineSessionController,
    private val routineRepository: RoutineRepository
) : SessionManagerRepository {

    private val mutex = Mutex()

    override suspend fun startSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId) ?: return@withLock
            sessionEventRepository.recordSessionEvent(
                sessionId = sessionId,
                sessionEvents = SessionEvents.START_SESSION,
                appPackage = null
            )
            val remainingSeconds = getRemainingSeconds(session)
            val totalSeconds = getTotalSeconds(session)
            routineSessionController.startSession(sessionId, remainingSeconds, totalSeconds)
            scheduleEnd(sessionId)
        }
    }

    override suspend fun stopSession(sessionId: String) {
        mutex.withLock {
            sessionEventRepository.recordSessionEvent(
                sessionId = sessionId,
                sessionEvents = SessionEvents.COMPLETE_SESSION,
                appPackage = null
            )
            sessionRepository.updateSessionStatus(
                sessionId = sessionId,
                status = SessionStatus.COMPLETED,
                actualEndTime = DateTimeUtils.getCurrentDateTime()
            )
            routineSessionController.stopSession(sessionId)
        }
    }

    override suspend fun pauseSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId)
            if (session != null) {
                val remaining = DateTimeUtils.getRemainingDuration(session.endTime)
                sessionRepository.updateSession(
                    session.copy(
                        pausedRemainingSeconds = remaining,
                        sessionStatus = SessionStatus.PAUSED
                    )
                )
                sessionEventRepository.recordSessionEvent(
                    sessionId = sessionId,
                    sessionEvents = SessionEvents.PAUSE_SESSION,
                    appPackage = null
                )
                cancelScheduledSession(sessionId)
                val totalSeconds = getTotalSeconds(session)
                routineSessionController.pauseSession(sessionId, remaining, totalSeconds)
            }
        }
    }

    override suspend fun resumeSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId)
            if (session != null) {
                val remainingSeconds = session.pausedRemainingSeconds ?: 0L

                val originalTotalSeconds = (session.endTime.toInstant(TimeZone.currentSystemDefault()) -
                        session.startTime.toInstant(TimeZone.currentSystemDefault())).inWholeSeconds

                val newEndTime = DateTimeUtils.getEndTimeForDuration(remainingSeconds)

                val newStartTime = (newEndTime.toInstant(TimeZone.currentSystemDefault()) - originalTotalSeconds.seconds)
                    .toLocalDateTime(TimeZone.currentSystemDefault())

                sessionRepository.updateSession(
                    session.copy(
                        pausedRemainingSeconds = null,
                        startTime = newStartTime,
                        endTime = newEndTime,
                        sessionStatus = SessionStatus.RUNNING
                    )
                )
                scheduleEnd(sessionId)
                sessionEventRepository.recordSessionEvent(
                    sessionId = sessionId,
                    sessionEvents = SessionEvents.RESUME_SESSION,
                    appPackage = null
                )
                val totalSeconds = originalTotalSeconds.coerceAtLeast(1L)
                routineSessionController.resumeRoutine(sessionId, remainingSeconds, totalSeconds)
            }
        }
    }


    override suspend fun logSessionEvent(
        sessionId: String,
        eventName: SessionEvents,
        appPackage: String?
    ) {
        mutex.withLock {
            sessionEventRepository.recordSessionEvent(
                sessionId = sessionId,
                sessionEvents = eventName,
                appPackage = appPackage
            )
        }
    }

    override suspend fun scheduleSession(routineId: String) {
        val routine = routineRepository.getRoutineById(routineId)
        if (routine != null) {
            val startTime = routine.startTime
            val scheduleTime = DateTimeUtils.getNextScheduledTime(startTime, routine.daysOfWeek)
            routineSessionController.scheduleRoutine(routineId, scheduleTime)
        }
    }

    override suspend fun scheduleEnd(sessionId: String) {
        val session = sessionRepository.getSessionById(sessionId)
        if (session != null) {
            val endTime = session.endTime
            routineSessionController.scheduleStopSession(sessionId, endTime)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createSession(
        routineId: String,
    ): String {
        val routine = routineRepository.getRoutineById(routineId)
        val sessionId = Uuid.generateV4().toString()
        val currentDateTime = DateTimeUtils.getCurrentDateTime()
        val routineEndTime = routine?.endTime
        val calculatedEndTime = if (routineEndTime != null) {
            val candidateDate = currentDateTime.date
            var endDateTime = LocalDateTime(
                year = candidateDate.year,
                month = candidateDate.month,
                day = candidateDate.day,
                hour = routineEndTime.hour,
                minute = routineEndTime.minute,
                second = routineEndTime.second,
                nanosecond = 0
            )
            if (endDateTime <= currentDateTime) {
                val nextDay = candidateDate.plus(1, DateTimeUnit.DAY)
                endDateTime = LocalDateTime(
                    year = nextDay.year,
                    month = nextDay.month,
                    day = nextDay.day,
                    hour = routineEndTime.hour,
                    minute = routineEndTime.minute,
                    second = routineEndTime.second,
                    nanosecond = 0
                )
            }
            endDateTime
        } else {
            currentDateTime
        }

        val sessionEntity = SessionEntity(
            id = sessionId,
            routineId = routineId,
            startTime = currentDateTime,
            endTime = calculatedEndTime,
            pausedRemainingSeconds = null,
            sessionStatus = SessionStatus.RUNNING,
            sessionType = SessionType.SCHEDULED,
            createdAt = currentDateTime,
            sessionName = routine?.name ?: "",
            unlockChallengeType = routine?.unlockChallengeType ?: UnlockChallengeType.STRICT_MODE,
            waitDurationSeconds = routine?.waitDurationSeconds ?: 0,
            blockedApps = routine?.disableAppPackages,
            actualStartTime = currentDateTime,
        )
        sessionRepository.insertSession(sessionEntity)
        startSession(sessionId)
        return sessionId
    }

    override suspend fun cancelScheduledRoutine(routineId: String) {
        routineSessionController.cancelRoutine(routineId)
    }

    override suspend fun cancelScheduledSession(sessionId: String) {
        routineSessionController.cancelScheduledSession(sessionId)
    }

    override suspend fun replaceSession(sessionId: String) {
        val session = sessionRepository.getSessionById(sessionId) ?: return
        val remainingSeconds = getRemainingSeconds(session)
        val totalSeconds = getTotalSeconds(session)
        routineSessionController.startSession(sessionId, remainingSeconds, totalSeconds)
        scheduleEnd(sessionId)
    }

    private fun getRemainingSeconds(session: SessionEntity): Long {
        return if (session.sessionStatus == SessionStatus.PAUSED && session.pausedRemainingSeconds != null) {
            session.pausedRemainingSeconds
        } else {
            DateTimeUtils.getRemainingDuration(session.endTime)
        }
    }

    private fun getTotalSeconds(session: SessionEntity): Long {
        return (session.endTime.toInstant(TimeZone.currentSystemDefault()) -
                session.startTime.toInstant(TimeZone.currentSystemDefault())).inWholeSeconds.coerceAtLeast(1L)
    }
}
