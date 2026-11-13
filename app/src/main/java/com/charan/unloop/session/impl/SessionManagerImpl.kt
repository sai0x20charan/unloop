package com.charan.unloop.session.impl

import android.content.Context
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.generateUuid
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import com.charan.unloop.session.SessionController
import com.charan.unloop.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class SessionManagerImpl @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val sessionController: SessionController,
    private val routineRepository: RoutineRepository,
    @ApplicationContext private val context: Context
) : SessionManager {

    private val mutex = Mutex()

    override suspend fun startInstantSession(
        name: String,
        durationSeconds: Long,
        blockedAppPackages: List<String>,
        areSessionControlsLocked : Boolean,
        unlockChallengeType: UnlockChallengeType,
        waitDurationSeconds : Int,
    ): String? = mutex.withLock {
        if (durationSeconds <= 0 || blockedAppPackages.isEmpty()) return@withLock null

        val startTime = DateTimeUtils.getCurrentDateTime()
        val session = SessionEntity(
            id = generateUuid(),
            actualStartTime = startTime,
            scheduledStartTime = startTime,
            scheduledEndTime = DateTimeUtils.getEndTimeForDuration(durationSeconds),
            blockedAppPackages = blockedAppPackages,
            status = SessionStatus.RUNNING,
            type = SessionType.INSTANT,
            name = name.trim().ifBlank {
                context.getString(R.string.home_session_title_default)
            },
            unlockChallengeType = unlockChallengeType,
            waitDurationSeconds = waitDurationSeconds,
            createdAt = startTime,
            areSessionControlsLocked = areSessionControlsLocked
        )
        sessionRepository.insertSession(session)
        startSession(session)
        session.id
    }

    private suspend fun startSession(session: SessionEntity) {
        sessionEventRepository.recordSessionEvent(
            sessionId = session.id,
            sessionEvents = SessionEventType.START_SESSION,
            packageName = null
        )
        sessionController.startSession(
            sessionId = session.id,
            sessionName = session.name,
            remainingSeconds = getRemainingSeconds(session),
            totalSeconds = getTotalSeconds(session),
            areSessionControlsLocked = session.areSessionControlsLocked
        )
        scheduleEnd(session.id)
    }

    override suspend fun stopSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId) ?: return@withLock
            if (session.status != SessionStatus.RUNNING &&
                session.status != SessionStatus.PAUSED
            ) return@withLock

            sessionEventRepository.recordSessionEvent(
                sessionId = sessionId,
                sessionEvents = SessionEventType.COMPLETE_SESSION,
                packageName = null
            )
            sessionRepository.updateSessionStatus(
                sessionId = sessionId,
                status = SessionStatus.COMPLETED,
                actualEndTime = DateTimeUtils.getCurrentDateTime()
            )
            cancelSessionEndAlarm(sessionId)
            if (session.routineId != null) {
                scheduleNextRoutineStart(session.routineId)
            }
            sessionController.stopSession(sessionId)
        }
    }

    override suspend fun pauseSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId)
            if (session != null && session.status == SessionStatus.RUNNING &&
                !session.areSessionControlsLocked
            ) {
                val remaining = DateTimeUtils.getRemainingSeconds(session.scheduledEndTime)
                sessionRepository.updateSession(
                    session.copy(
                        pausedRemainingSeconds = remaining,
                        status = SessionStatus.PAUSED
                    )
                )
                sessionEventRepository.recordSessionEvent(
                    sessionId = sessionId,
                    sessionEvents = SessionEventType.PAUSE_SESSION,
                    packageName = null
                )
                cancelSessionEndAlarm(sessionId)
                val totalSeconds = getTotalSeconds(session)
                sessionController.pauseSession(
                    sessionId = sessionId,
                    sessionName = session.name,
                    remainingSeconds = remaining,
                    totalSeconds = totalSeconds
                )
            }
        }
    }

    override suspend fun resumeSession(sessionId: String) {
        mutex.withLock {
            val session = sessionRepository.getSessionById(sessionId)
            if (session != null && session.status == SessionStatus.PAUSED &&
                !session.areSessionControlsLocked
            ) {
                val remainingSeconds = session.pausedRemainingSeconds ?: 0L

                val originalTotalSeconds = DateTimeUtils.getDurationSeconds(
                    startTime = session.scheduledStartTime,
                    endTime = session.scheduledEndTime
                )

                val newEndTime = DateTimeUtils.getEndTimeForDuration(remainingSeconds)

                val newStartTime = DateTimeUtils.getStartTimeForDuration(
                    endTime = newEndTime,
                    seconds = originalTotalSeconds
                )

                sessionRepository.updateSession(
                    session.copy(
                        pausedRemainingSeconds = null,
                        scheduledStartTime = newStartTime,
                        scheduledEndTime = newEndTime,
                        status = SessionStatus.RUNNING
                    )
                )
                scheduleEnd(sessionId)
                sessionEventRepository.recordSessionEvent(
                    sessionId = sessionId,
                    sessionEvents = SessionEventType.RESUME_SESSION,
                    packageName = null
                )
                val totalSeconds = originalTotalSeconds.coerceAtLeast(1L)
                sessionController.resumeSession(
                    sessionId = sessionId,
                    sessionName = session.name,
                    remainingSeconds = remainingSeconds,
                    totalSeconds = totalSeconds
                )
            }
        }
    }


    override suspend fun logSessionEvent(
        sessionId: String,
        eventName: SessionEventType,
        packageName: String?
    ) {
        mutex.withLock {
            sessionEventRepository.recordSessionEvent(
                sessionId = sessionId,
                sessionEvents = eventName,
                packageName = packageName
            )
        }
    }

    override suspend fun scheduleNextRoutineStart(routineId: String) {
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
            val endTime = session.scheduledEndTime
            sessionController.scheduleStopSession(sessionId, endTime)
        }
    }

    override suspend fun startScheduledSession(
        routineId: String,
    ): String? = mutex.withLock {
        val routine = routineRepository.getRoutineById(routineId) ?: return@withLock null
        if (!routine.isEnabled || routine.isDeleted) return@withLock null
        val sessionId = generateUuid()
        val currentDateTime = DateTimeUtils.getCurrentDateTime()
        val calculatedEndTime = DateTimeUtils.getScheduledEndTime(
            endTime = routine.endTime,
            startTime = currentDateTime
        )

        val sessionEntity = SessionEntity(
            id = sessionId,
            routineId = routineId,
            scheduledStartTime = currentDateTime,
            scheduledEndTime = calculatedEndTime,
            pausedRemainingSeconds = null,
            status = SessionStatus.RUNNING,
            type = SessionType.SCHEDULED,
            createdAt = currentDateTime,
            name = routine.name,
            unlockChallengeType = routine.unlockChallengeType,
            waitDurationSeconds = routine.waitDurationSeconds ?: 0,
            blockedAppPackages = routine.blockedAppPackages,
            actualStartTime = currentDateTime,
            areSessionControlsLocked = routine.areSessionControlsLocked,
        )
        sessionRepository.insertSession(sessionEntity)
        scheduleNextRoutineStart(routineId)
        startSession(sessionEntity)
        sessionId
    }

    override suspend fun cancelScheduledRoutine(routineId: String) {
        sessionController.cancelRoutine(routineId)
    }

    override suspend fun cancelSessionEndAlarm(sessionId: String) {
        sessionController.cancelSessionEndAlarm(sessionId)
    }

    override suspend fun restoreSession(sessionId: String) {
        val session = sessionRepository.getSessionById(sessionId) ?: return
        val remainingSeconds = getRemainingSeconds(session)
        val totalSeconds = getTotalSeconds(session)
        sessionController.startSession(
            sessionId = sessionId,
            sessionName = session.name,
            remainingSeconds = remainingSeconds,
            totalSeconds = totalSeconds,
            areSessionControlsLocked = session.areSessionControlsLocked
        )
        scheduleEnd(sessionId)
    }

    private fun getRemainingSeconds(session: SessionEntity): Long {
        return if (session.status == SessionStatus.PAUSED && session.pausedRemainingSeconds != null) {
            session.pausedRemainingSeconds
        } else {
            DateTimeUtils.getRemainingSeconds(session.scheduledEndTime)
        }
    }

    private fun getTotalSeconds(session: SessionEntity): Long {
        return DateTimeUtils.getDurationSeconds(
            startTime = session.scheduledStartTime,
            endTime = session.scheduledEndTime
        ).coerceAtLeast(1L)
    }
}
