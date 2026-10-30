package com.charan.unloop.session.impl

import android.content.Context
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.routine.UnlockChallengeType
import com.charan.unloop.session.SessionController
import com.charan.unloop.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SessionManagerImpl @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val sessionController: SessionController,
    private val routineRepository: RoutineRepository,
    @ApplicationContext private val context: Context
) : SessionManager {

    private val mutex = Mutex()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun startInstantSession(
        name: String,
        durationSeconds: Long,
        blockedApps: List<String>
    ): String? = mutex.withLock {
        if (durationSeconds <= 0 || blockedApps.isEmpty()) return@withLock null

        val startTime = DateTimeUtils.getCurrentDateTime()
        val session = SessionEntity(
            id = Uuid.generateV4().toString(),
            actualStartTime = startTime,
            startTime = startTime,
            endTime = DateTimeUtils.getEndTimeForDuration(durationSeconds),
            blockedApps = blockedApps,
            sessionStatus = SessionStatus.RUNNING,
            sessionType = SessionType.INSTANT,
            sessionName = name.trim().ifBlank {
                context.getString(R.string.home_session_title_default)
            },
            unlockChallengeType = UnlockChallengeType.STRICT_MODE,
            createdAt = startTime
        )
        sessionRepository.insertSession(session)
        startSession(session)
        session.id
    }

    private suspend fun startSession(session: SessionEntity) {
        sessionEventRepository.recordSessionEvent(
            sessionId = session.id,
            sessionEvents = SessionEvents.START_SESSION,
            appPackage = null
        )
        sessionController.startSession(
            sessionId = session.id,
            sessionName = session.sessionName,
            remainingSeconds = getRemainingSeconds(session),
            totalSeconds = getTotalSeconds(session),
            areSessionControlsLocked = session.areSessionControlsLocked
        )
        scheduleEnd(session.id)
    }

    override suspend fun stopSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId) ?: return@withLock
            if (session.sessionStatus != SessionStatus.RUNNING &&
                session.sessionStatus != SessionStatus.PAUSED
            ) return@withLock

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
            cancelScheduledSession(sessionId)
            if (session.routineId != null) {
                scheduleSession(session.routineId)
            }
            sessionController.stopSession(sessionId)
        }
    }

    override suspend fun pauseSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId)
            if (session != null && session.sessionStatus == SessionStatus.RUNNING &&
                !session.areSessionControlsLocked
            ) {
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
                sessionController.pauseSession(
                    sessionId = sessionId,
                    sessionName = session.sessionName,
                    remainingSeconds = remaining,
                    totalSeconds = totalSeconds
                )
            }
        }
    }

    override suspend fun resumeSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId)
            if (session != null && session.sessionStatus == SessionStatus.PAUSED &&
                !session.areSessionControlsLocked
            ) {
                val remainingSeconds = session.pausedRemainingSeconds ?: 0L

                val originalTotalSeconds = DateTimeUtils.getDurationSeconds(
                    startTime = session.startTime,
                    endTime = session.endTime
                )

                val newEndTime = DateTimeUtils.getEndTimeForDuration(remainingSeconds)

                val newStartTime = DateTimeUtils.getStartTimeForDuration(
                    endTime = newEndTime,
                    seconds = originalTotalSeconds
                )

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
                sessionController.resumeSession(
                    sessionId = sessionId,
                    sessionName = session.sessionName,
                    remainingSeconds = remainingSeconds,
                    totalSeconds = totalSeconds
                )
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
        if (routine != null && routine.isEnabled && !routine.isDeleted && routine.daysOfWeek.isNotEmpty()) {
            val startTime = routine.startTime
            val scheduleTime = DateTimeUtils.getNextScheduledTime(startTime, routine.daysOfWeek)
            sessionController.scheduleRoutine(routineId, scheduleTime)
        }
    }

    override suspend fun scheduleEnd(sessionId: String) {
        val session = sessionRepository.getSessionById(sessionId)
        if (session != null) {
            val endTime = session.endTime
            sessionController.scheduleStopSession(sessionId, endTime)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun startScheduledSession(
        routineId: String,
    ): String? = mutex.withLock {
        val routine = routineRepository.getRoutineById(routineId) ?: return@withLock null
        val sessionId = Uuid.generateV4().toString()
        val currentDateTime = DateTimeUtils.getCurrentDateTime()
        val calculatedEndTime = DateTimeUtils.getScheduledEndTime(
            endTime = routine.endTime,
            startTime = currentDateTime
        )

        val sessionEntity = SessionEntity(
            id = sessionId,
            routineId = routineId,
            startTime = currentDateTime,
            endTime = calculatedEndTime,
            pausedRemainingSeconds = null,
            sessionStatus = SessionStatus.RUNNING,
            sessionType = SessionType.SCHEDULED,
            createdAt = currentDateTime,
            sessionName = routine.name,
            unlockChallengeType = routine.unlockChallengeType,
            waitDurationSeconds = routine.waitDurationSeconds ?: 0,
            blockedApps = routine.disableAppPackages,
            actualStartTime = currentDateTime,
            areSessionControlsLocked = routine.areSessionControlsLocked,
        )
        sessionRepository.insertSession(sessionEntity)
        scheduleSession(routineId)
        startSession(sessionEntity)
        sessionId
    }

    override suspend fun cancelScheduledRoutine(routineId: String) {
        sessionController.cancelRoutine(routineId)
    }

    override suspend fun cancelScheduledSession(sessionId: String) {
        sessionController.cancelScheduledSession(sessionId)
    }

    override suspend fun restoreSession(sessionId: String) {
        val session = sessionRepository.getSessionById(sessionId) ?: return
        val remainingSeconds = getRemainingSeconds(session)
        val totalSeconds = getTotalSeconds(session)
        sessionController.startSession(
            sessionId = sessionId,
            sessionName = session.sessionName,
            remainingSeconds = remainingSeconds,
            totalSeconds = totalSeconds,
            areSessionControlsLocked = session.areSessionControlsLocked
        )
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
        return DateTimeUtils.getDurationSeconds(
            startTime = session.startTime,
            endTime = session.endTime
        ).coerceAtLeast(1L)
    }
}
