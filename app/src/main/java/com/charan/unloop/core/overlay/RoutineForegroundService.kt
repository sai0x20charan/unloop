package com.charan.unloop.core.overlay

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.DateTimeUtils.toLocalDateTime
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.service.FullScreenOverlayService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@AndroidEntryPoint
class RoutineForegroundService : Service() {

    @Inject
    lateinit var appUsageChecker: AppUsageChecker

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var routineRepository: RoutineRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var routineSessionController: RoutineSessionController

    private var activeSession: SessionEntity? = null
    private var monitoringJob: Job? = null

    companion object {
        const val ACTION_STOP_ROUTINE = "com.charan.unloop.ACTION_STOP_ROUTINE"
        const val ACTION_PAUSE_ROUTINE = "com.charan.unloop.ACTION_PAUSE_ROUTINE"
        const val ACTION_RESUME_ROUTINE = "com.charan.unloop.ACTION_RESUME_ROUTINE"
        const val EXTRA_SESSION_ID = "sessionId"
        const val EXTRA_ROUTINE_ID = "routineId"
        const val EXTRA_REMAINING_SECONDS = "remainingSeconds"
    }

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val sessionId = intent?.getStringExtra(EXTRA_SESSION_ID)
        val routineId = intent?.getStringExtra(EXTRA_ROUTINE_ID)
        val action = intent?.action

        if (action == ACTION_PAUSE_ROUTINE) {
            pauseSession(sessionId ?: activeSession?.id ?: return START_NOT_STICKY)
            return START_NOT_STICKY
        }

        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            notificationHelper.buildForegroundNotification()
        )

        if (action == ACTION_STOP_ROUTINE) {
            completeAndStopSession(sessionId)
            return START_NOT_STICKY
        }

        if (action == ACTION_RESUME_ROUTINE) {
            resumeAndStartMonitoring(sessionId ?: activeSession?.id)
            return START_REDELIVER_INTENT
        }

        startMonitoring(sessionId = sessionId, routineId = routineId)

        return START_REDELIVER_INTENT
    }

    private fun completeAndStopSession(sessionId: String?) {
        monitoringJob?.cancel()
        serviceScope.launch {
            val targetId = sessionId
                ?: activeSession?.id
                ?: sessionRepository.getActiveSessionOnce()?.id

            if (!targetId.isNullOrBlank()) {
                sessionRepository.updateSessionStatus(
                    sessionId = targetId,
                    status = SessionStatus.COMPLETED,
                    actualEndTime = DateTimeUtils.getCurrentDateTime()
                )
                routineSessionController.cancelStopRoutine(targetId)
            }
            stopOverlayAndSelf()
        }
    }

    private fun pauseSession(sessionId: String) {
        monitoringJob?.cancel()
        removeOverLay()
        routineSessionController.cancelStopRoutine(sessionId)
        serviceScope.launch {
            try {
                val session = sessionRepository.getSessionById(sessionId) ?: return@launch
                val remainingSeconds = DateTimeUtils.getRemainingDuration(session.endTime)
                sessionRepository.updateSession(
                    session.copy(
                        sessionStatus = SessionStatus.PAUSED,
                        pausedRemainingSeconds = remainingSeconds
                    )
                )
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun resumeAndStartMonitoring(sessionId: String?) {
        if (sessionId.isNullOrBlank()) return
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            val session = sessionRepository.getSessionById(sessionId) ?: return@launch
            val remainingSeconds = session.pausedRemainingSeconds ?: return@launch
            val now = DateTimeUtils.getCurrentDateTime()
            val newEndTime = DateTimeUtils.getEndTimeForDuration(remainingSeconds)

            val originalTotalSeconds = (session.endTime.toInstant(TimeZone.currentSystemDefault()) -
                session.startTime.toInstant(TimeZone.currentSystemDefault())).inWholeSeconds
            val elapsedSeconds = (originalTotalSeconds - remainingSeconds).coerceAtLeast(0)
            val newStartTime = (now.toInstant(TimeZone.currentSystemDefault()) - elapsedSeconds.seconds).toLocalDateTime(TimeZone.currentSystemDefault())

            val updatedSession = session.copy(
                sessionStatus = SessionStatus.RUNNING,
                startTime = newStartTime,
                endTime = newEndTime,
                pausedRemainingSeconds = null
            )
            sessionRepository.updateSession(updatedSession)
            activeSession = updatedSession

            routineSessionController.scheduleStopRoutine(
                sessionId = updatedSession.id,
                endTime = updatedSession.endTime
            )

            startMonitoringLoop(updatedSession)
        }
    }

    private fun updateSessionStatus(sessionId: String, sessionStatus: SessionStatus) = serviceScope.launch {
        sessionRepository.updateSessionStatus(
            sessionId = sessionId,
            status = sessionStatus,
            actualEndTime = null
        )
    }

    private fun removeOverLay() {
        stopService(Intent(this, FullScreenOverlayService::class.java))
    }

    private fun stopOverlayAndSelf() {
        stopService(Intent(this, FullScreenOverlayService::class.java))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startMonitoring(sessionId: String?, routineId: String?) {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            val session = when {
                !sessionId.isNullOrBlank() -> sessionRepository.getSessionById(sessionId)
                !routineId.isNullOrBlank() -> createOrGetScheduledSession(routineId)
                else -> sessionRepository.getActiveSessionOnce()
            } ?: return@launch
            activeSession = session

            routineSessionController.scheduleStopRoutine(
                sessionId = session.id,
                endTime = session.endTime
            )

            startMonitoringLoop(session)
        }
    }

    private fun startMonitoringLoop(session: SessionEntity) {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                val currentSession = sessionRepository.getSessionById(session.id)
                val currentEndTime = currentSession?.endTime ?: session.endTime
                val remainingSeconds = DateTimeUtils.getRemainingDuration(
                    endTime = currentEndTime
                )

                if (remainingSeconds <= 0L) {
                    sessionRepository.updateSessionStatus(
                        sessionId = session.id,
                        status = SessionStatus.COMPLETED,
                        actualEndTime = DateTimeUtils.getCurrentDateTime()
                    )
                    routineSessionController.cancelStopRoutine(session.id)
                    stopOverlayAndSelf()
                    break
                }

                val foregroundPackage = appUsageChecker.getForegroundApp()
                if (foregroundPackage != null) {
                    if (session.blockedApps?.contains(foregroundPackage) == true) {
                        val overlayIntent = Intent(
                            this@RoutineForegroundService,
                            FullScreenOverlayService::class.java
                        ).apply {
                            putExtra(
                                FullScreenOverlayService.EXTRA_UNLOCK_TYPE,
                                session.unlockChallengeType.name
                            )
                            putExtra(
                                FullScreenOverlayService.EXTRA_ROUTINE_ID,
                                session.id
                            )
                            putExtra(
                                FullScreenOverlayService.EXTRA_ROUTINE_NAME,
                                session.sessionName
                            )
                            putExtra(
                                FullScreenOverlayService.EXTRA_APP_PACKAGE,
                                foregroundPackage
                            )
                            putExtra(
                                FullScreenOverlayService.EXTRA_WAIT_SECONDS,
                                session.waitDurationSeconds ?: 30
                            )
                            putExtra(
                                FullScreenOverlayService.EXTRA_END_TIME_SECOND_OF_DAY,
                                currentEndTime.time.toSecondOfDay()
                            )
                        }
                        startService(overlayIntent)
                    } else {
                        removeOverLay()
                    }
                }

                delay(1000.milliseconds)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createOrGetScheduledSession(routineId: String): SessionEntity? {
        val existingActive = sessionRepository.getActiveSessionOnce()
        if (existingActive != null && existingActive.routineId == routineId) {
            return existingActive
        }

        val routine = routineRepository.getRoutineById(routineId) ?: return null
        if (!routine.isEnabled || routine.isDeleted) return null

        val nowTime = DateTimeUtils.getCurrentDateTime()
        val routineStartTime = routine.startTime?.toLocalDateTime() ?: nowTime
        val routineEndTime = if (routine.endTime != null && routine.startTime != null && routine.endTime <= routine.startTime) {
            val nextDate = nowTime.date.plus(1, DateTimeUnit.DAY)
            LocalDateTime(
                year = nextDate.year,
                month = nextDate.month,
                day = nextDate.day,
                hour = routine.endTime.hour,
                minute = routine.endTime.minute,
                second = routine.endTime.second,
                nanosecond = 0
            )
        } else {
            routine.endTime?.toLocalDateTime() ?: nowTime
        }

        val newSession = SessionEntity(
            id = Uuid.generateV4().toString(),
            routineId = routine.id,
            actualStartTime = nowTime,
            startTime = routineStartTime,
            endTime = routineEndTime,
            sessionStatus = SessionStatus.RUNNING,
            sessionType = SessionType.SCHEDULED,
            blockedApps = routine.disableAppPackages,
            unlockChallengeType = routine.unlockChallengeType,
            sessionName = routine.name,
            waitDurationSeconds = routine.waitDurationSeconds,
            createdAt = nowTime
        )
        sessionRepository.insertSession(newSession)

        if (!routine.daysOfWeek.isNullOrEmpty() && routine.startTime != null) {
            routineSessionController.scheduleRoutine(
                routineId = routine.id,
                startTime = routine.startTime,
                frequency = routine.daysOfWeek,
                isAllDay = routine.isAllDay
            )
        }

        return newSession
    }

    override fun onDestroy() {
        stopService(Intent(this, FullScreenOverlayService::class.java))
        serviceScope.cancel()
        super.onDestroy()
    }
}
