package com.charan.unloop.core.overlay

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.DateTimeUtils.toLocalDateTime
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionManagerRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.service.FullScreenOverlayService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
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
    lateinit var sessionManagerRepository: SessionManagerRepository

    @Inject
    lateinit var routineRepository: RoutineRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var routineSessionController: RoutineSessionController

    @Inject
    lateinit var sessionEventRepository: SessionEventRepository

    private var sessionId : String? = null
    private var monitoringJob: Job? = null

    companion object {

        const val ACTION_START_ROUTINE = "com.charan.unloop.ACTION_START_ROUTINE"
        const val ACTION_STOP_SESSION = "com.charan.unloop.ACTION_STOP_SESSION"
        const val ACTION_PAUSE_SESSION = "com.charan.unloop.ACTION_PAUSE_SESSION"
        const val ACTION_RESUME_SESSION = "com.charan.unloop.ACTION_RESUME_SESSION"
        const val ACTION_START_SESSION = "com.charan.unloop.ACTION_START_SESSION"
        const val ACTION_REQUEST_START_ROUTINE = "com.charan.unloop.REQUEST_START_ROUTINE"
        const val ACTION_REQUEST_STOP_SESSION = "com.charan.unloop.REQUEST_STOP_SESSION"
        const val ACTION_REQUEST_PAUSE_SESSION = "com.charan.unloop.REQUEST_PAUSE_SESSION"
        const val ACTION_REQUEST_RESUME_SESSION = "com.charan.unloop.REQUEST_RESUME_SESSION"
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
        val notification = notificationHelper.buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                NotificationHelper.NOTIFICATION_ID,
                notification
            )
        }

        sessionId = intent?.getStringExtra(EXTRA_SESSION_ID)

        val routineId = intent?.getStringExtra(EXTRA_ROUTINE_ID)
        val action = intent?.action

        when (action) {
            ACTION_START_SESSION -> {
                startMonitoringLoop()
            }
            ACTION_PAUSE_SESSION -> {
                pauseSession()
                return START_REDELIVER_INTENT
            }
            ACTION_STOP_SESSION -> {
                stopOverlayAndSelf()
                return START_NOT_STICKY
            }
            ACTION_RESUME_SESSION -> {
                resumeSession()
                return START_REDELIVER_INTENT
            }

            ACTION_REQUEST_PAUSE_SESSION ->{
                serviceScope.launch {
                    sessionManagerRepository.pauseSession(sessionId ?: return@launch)
                    return@launch
                }
                return START_NOT_STICKY
            }

            ACTION_REQUEST_STOP_SESSION ->{
                serviceScope.launch {
                    sessionManagerRepository.stopSession(sessionId ?: return@launch)
                    return@launch
                }
                return START_NOT_STICKY

            }

            ACTION_REQUEST_RESUME_SESSION ->{
                serviceScope.launch {
                    sessionManagerRepository.resumeSession(sessionId ?: return@launch)
                    return@launch
                }
                return START_NOT_STICKY
            }

            ACTION_REQUEST_START_ROUTINE, ACTION_START_ROUTINE -> {
                serviceScope.launch {
                    val createdSessionId = sessionManagerRepository.createSession(
                        routineId = routineId ?: return@launch,
                    )
                    sessionId = createdSessionId
                    startMonitoringLoop()
                    return@launch
                }
                return START_NOT_STICKY
            }
            else -> {
                stopOverlayAndSelf()
                return START_NOT_STICKY
            }
        }

        return START_REDELIVER_INTENT
    }

    private fun pauseSession() {
        monitoringJob?.cancel()
        removeOverLay()
    }

    private fun resumeSession() {
        if (sessionId.isNullOrBlank()) return
        monitoringJob?.cancel()
        startMonitoringLoop()
    }

    private fun removeOverLay() {
        stopService(Intent(this, FullScreenOverlayService::class.java))
    }

    private fun stopOverlayAndSelf() {
        stopService(Intent(this, FullScreenOverlayService::class.java))
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    private fun observeForegroundApp(): Flow<String?> = flow {
        while (currentCoroutineContext().isActive) {
            emit(appUsageChecker.getForegroundApp())
            delay(1.seconds)
        }
    }.distinctUntilChanged()

    private fun startMonitoringLoop() {
        monitoringJob?.cancel()

        monitoringJob = serviceScope.launch {
            observeForegroundApp().collect { foregroundPackage ->
                if (foregroundPackage.isNullOrBlank()) return@collect
                val currentSession = sessionRepository
                    .getSessionById(sessionId ?: return@collect)
                    ?: return@collect

                val blockedApps = currentSession.blockedApps.orEmpty()

                if (foregroundPackage in blockedApps) {
                    sessionManagerRepository.logSessionEvent(
                        sessionId = currentSession.id,
                        eventName = SessionEvents.APP_BLOCKED,
                        appPackage = foregroundPackage
                    )
                    val overlayIntent = Intent(
                        this@RoutineForegroundService,
                        FullScreenOverlayService::class.java
                    ).apply {
                        putExtra(
                            FullScreenOverlayService.EXTRA_UNLOCK_TYPE,
                            currentSession.unlockChallengeType.name
                        )
                        putExtra(
                            FullScreenOverlayService.EXTRA_ROUTINE_ID,
                            currentSession.id
                        )
                        putExtra(
                            FullScreenOverlayService.EXTRA_ROUTINE_NAME,
                            currentSession.sessionName
                        )
                        putExtra(
                            FullScreenOverlayService.EXTRA_APP_PACKAGE,
                            foregroundPackage
                        )
                        putExtra(
                            FullScreenOverlayService.EXTRA_WAIT_SECONDS,
                            currentSession.waitDurationSeconds ?: 30
                        )
                        putExtra(
                            FullScreenOverlayService.EXTRA_END_TIME_SECOND_OF_DAY,
                            currentSession.endTime.toString()
                        )
                    }
                    startService(overlayIntent)
                } else {
                    removeOverLay()
                }
            }
        }
    }

    override fun onDestroy() {
        stopService(Intent(this, FullScreenOverlayService::class.java))
        serviceScope.cancel()
        super.onDestroy()
    }
}
