package com.charan.unloop.session

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.ServiceCompat
import com.charan.unloop.R
import com.charan.unloop.session.blocking.AppUsageChecker
import com.charan.unloop.session.blocking.FullScreenOverlayService
import com.charan.unloop.core.notification.NotificationHelper
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.data.repository.SessionRepository
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
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class SessionForegroundService : Service() {

    @Inject
    lateinit var appUsageChecker: AppUsageChecker

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private var sessionId : String? = null
    private var sessionName: String? = null
    private var monitoringJob: Job? = null
    private var notificationJob: Job? = null
    private var currentNotification: Notification? = null
    private var areSessionControlsLocked = true

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
        const val EXTRA_SESSION_NAME = "sessionName"
        const val EXTRA_ROUTINE_ID = "routineId"
        const val EXTRA_REMAINING_SECONDS = "remainingSeconds"
        const val EXTRA_TOTAL_SECONDS = "totalSeconds"
        const val EXTRA_SESSION_CONTROLS_LOCKED = "sessionControlsLocked"
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
        sessionId = intent?.getStringExtra(EXTRA_SESSION_ID) ?: sessionId
        sessionName = intent?.getStringExtra(EXTRA_SESSION_NAME) ?: sessionName
        val action = intent?.action
        if (action == ACTION_START_SESSION) {
            areSessionControlsLocked = intent.getBooleanExtra(EXTRA_SESSION_CONTROLS_LOCKED, true)
        } else if (action == ACTION_PAUSE_SESSION || action == ACTION_RESUME_SESSION) {
            // Only unlocked sessions receive pause and resume commands from the manager.
            areSessionControlsLocked = false
        }
        val remainingSeconds = intent?.takeIf { it.hasExtra(EXTRA_REMAINING_SECONDS) }
            ?.getLongExtra(EXTRA_REMAINING_SECONDS, 0L)
        val totalSeconds = intent?.takeIf { it.hasExtra(EXTRA_TOTAL_SECONDS) }
            ?.getLongExtra(EXTRA_TOTAL_SECONDS, 0L)
        val notification = if (currentNotification == null ||
            action == ACTION_START_SESSION ||
            action == ACTION_PAUSE_SESSION ||
            action == ACTION_RESUME_SESSION
        ) {
            notificationHelper.buildForegroundNotification(
                title = sessionName ?: getString(R.string.home_session_title_default),
                sessionId = sessionId,
                remainingSeconds = remainingSeconds,
                totalTime = totalSeconds,
                isPaused = action == ACTION_PAUSE_SESSION,
                areSessionControlsLocked = areSessionControlsLocked
            )
        } else {
            currentNotification!!
        }
        currentNotification = notification
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
        val routineId = intent?.getStringExtra(EXTRA_ROUTINE_ID)

        when (action) {
            ACTION_START_SESSION -> {
                startMonitoringLoop()
            }
            ACTION_PAUSE_SESSION -> {
                pauseSession()
            }
            ACTION_STOP_SESSION -> {
                stopOverlayAndSelf()
                return START_NOT_STICKY
            }
            ACTION_RESUME_SESSION -> {
                resumeSession()
            }

            ACTION_REQUEST_PAUSE_SESSION ->{
                serviceScope.launch {
                    sessionManager.pauseSession(sessionId ?: return@launch)
                    return@launch
                }
                return START_NOT_STICKY
            }

            ACTION_REQUEST_STOP_SESSION ->{
                serviceScope.launch {
                    sessionManager.stopSession(sessionId ?: return@launch)
                    return@launch
                }
                return START_NOT_STICKY

            }

            ACTION_REQUEST_RESUME_SESSION ->{
                serviceScope.launch {
                    sessionManager.resumeSession(sessionId ?: return@launch)
                    return@launch
                }
                return START_NOT_STICKY
            }

            ACTION_REQUEST_START_ROUTINE, ACTION_START_ROUTINE -> {
                serviceScope.launch {
                    sessionManager.startScheduledSession(
                        routineId = routineId ?: return@launch,
                    )
                    return@launch
                }
                return START_NOT_STICKY
            }
            else -> {
                stopOverlayAndSelf()
                return START_NOT_STICKY
            }
        }
        if (action == ACTION_START_SESSION || action == ACTION_RESUME_SESSION) {
            startNotificationUpdates(remainingSeconds, totalSeconds)
        }
        return START_REDELIVER_INTENT
    }

    private fun startNotificationUpdates(
        remainingSeconds: Long?,
        totalSeconds: Long?
    ) {
        notificationJob?.cancel()
        val currentSessionId = sessionId ?: return
        if (remainingSeconds == null || totalSeconds == null) return
        val endTime = SystemClock.elapsedRealtime() + remainingSeconds * 1000L

        notificationJob = serviceScope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                delay(1.seconds)
                val remainingMillis = (endTime - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                val remainingTime = (remainingMillis + 999L) / 1000L
                currentNotification = notificationHelper.updateNotification(
                    title = sessionName ?: getString(R.string.home_session_title_default),
                    sessionId = currentSessionId,
                    remainingSeconds = remainingTime,
                    totalTime = totalSeconds,
                    areSessionControlsLocked = areSessionControlsLocked
                )
                if (remainingTime == 0L) break
            }
        }
    }

    private fun pauseSession() {
        monitoringJob?.cancel()
        notificationJob?.cancel()
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
        notificationJob?.cancel()
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
                if (foregroundPackage.isNullOrBlank()) {
                    removeOverLay()
                    return@collect
                }
                val currentSession = sessionRepository
                    .getSessionById(sessionId ?: return@collect)
                    ?: return@collect

                val blockedAppPackages = currentSession.blockedAppPackages.orEmpty()

                if (foregroundPackage in blockedAppPackages) {
                    sessionManager.logSessionEvent(
                        sessionId = currentSession.id,
                        eventName = SessionEventType.APP_BLOCKED,
                        packageName = foregroundPackage
                    )
                    val overlayIntent = Intent(
                        this@SessionForegroundService,
                        FullScreenOverlayService::class.java
                    ).apply {
                        putExtra(
                            FullScreenOverlayService.EXTRA_UNLOCK_TYPE,
                            currentSession.unlockChallengeType.name
                        )
                        putExtra(
                            FullScreenOverlayService.EXTRA_SESSION_ID,
                            currentSession.id
                        )
                        putExtra(
                            FullScreenOverlayService.EXTRA_ROUTINE_NAME,
                            currentSession.name
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
                            FullScreenOverlayService.EXTRA_END_TIME,
                            currentSession.scheduledEndTime.toDisplayString()
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
