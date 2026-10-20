package com.charan.unloop.core.overlay

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.charan.unloop.core.utils.DateUtils
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
import kotlinx.datetime.LocalTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
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
    lateinit var routineScheduler: RoutineScheduler

    private var activeSession: SessionEntity? = null
    private var monitoringJob: Job? = null

    companion object {
        const val ACTION_STOP_ROUTINE = "com.charan.unloop.ACTION_STOP_ROUTINE"

        const val ACTION_PAUSE_ROUTINE = "com.charan.unloop.ACTION_PAUSE_ROUTINE"

        const val ACTION_RESUME_ROUTINE = "com.charan.unloop.ACTION_RESUME_ROUTINE"
        const val EXTRA_SESSION_ID = "sessionId"
        const val EXTRA_ROUTINE_ID = "routineId"
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
        if(action == ACTION_PAUSE_ROUTINE){
            monitoringJob?.cancel()
            removeOverLay()
            updateSessionStatus(sessionId ?: activeSession?.id ?: return START_NOT_STICKY, SessionStatus.PAUSED)
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
                    actualEndTime = DateUtils.getCurrentTime()
                )
                routineScheduler.cancelStopRoutine(targetId)
            }
            stopOverlayAndSelf()
        }
    }

    private fun updateSessionStatus(sessionId : String, sessionStatus: SessionStatus) = serviceScope.launch{
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
            }
            updateSessionStatus(sessionId = session?.id ?: return@launch, sessionStatus = SessionStatus.RUNNING)

            activeSession = session

            routineScheduler.scheduleStopRoutine(
                sessionId = session.id,
                endTime = session.endTime
            )

            while (isActive) {
                val sessionStatus = sessionRepository.getSessionById(session.id)?.sessionStatus
                if(sessionStatus == SessionStatus.PAUSED){
                    removeOverLay()
                    continue
                }
                val remainingSeconds = DateUtils.getRemainingDuration(
                    startTime = session.startTime,
                    endTime = session.endTime
                )

                if (remainingSeconds <= 0L) {
                    sessionRepository.updateSessionStatus(
                        sessionId = session.id,
                        status = SessionStatus.COMPLETED,
                        actualEndTime = DateUtils.getCurrentTime()
                    )
                    routineScheduler.cancelStopRoutine(session.id)
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
                                session.endTime.toSecondOfDay()
                            )
                        }
                        startService(overlayIntent)
                    } else {
                        stopService(Intent(this@RoutineForegroundService, FullScreenOverlayService::class.java))
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

        val nowTime = DateUtils.getCurrentTime()
        val newSession = SessionEntity(
            id = Uuid.generateV4().toString(),
            routineId = routine.id,
            actualStartTime = nowTime,
            startTime = routine.startTime ?: nowTime,
            endTime = routine.endTime ?: LocalTime(23, 59, 59),
            sessionStatus = SessionStatus.RUNNING,
            sessionType = SessionType.SCHEDULED,
            blockedApps = routine.disableAppPackages,
            unlockChallengeType = routine.unlockChallengeType,
            sessionName = routine.name,
            waitDurationSeconds = routine.waitDurationSeconds
        )
        sessionRepository.insertSession(newSession)

        if (!routine.daysOfWeek.isNullOrEmpty() && routine.startTime != null) {
            routineScheduler.scheduleRoutine(
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
