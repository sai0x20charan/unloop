package com.charan.unloop.session.impl

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.charan.unloop.session.SessionController
import com.charan.unloop.session.SessionForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject

class SessionControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionController {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleRoutine(
        routineId: String,
        scheduleTime: LocalDateTime,
    ) {
        val pendingIntent = createRoutinePendingIntent(routineId) {
            putExtra(SessionForegroundService.EXTRA_ROUTINE_ID, routineId)
            action = SessionForegroundService.ACTION_REQUEST_START_ROUTINE
        }
        setExactAlarmSafe(
            scheduleTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            pendingIntent
        )
    }

    override fun scheduleStopSession(
        sessionId: String,
        endTime: LocalDateTime
    ) {
        if (sessionId.isBlank()) return
        val pendingIntent = createSessionPendingIntent(sessionId) {
            putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
            action = SessionForegroundService.ACTION_REQUEST_STOP_SESSION
        }
        setExactAlarmSafe(
            endTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            pendingIntent
        )
    }

    override fun cancelRoutine(routineId: String) {
        val pendingIntent = createRoutinePendingIntent(routineId) {
            putExtra(SessionForegroundService.EXTRA_ROUTINE_ID, routineId)
            action = SessionForegroundService.ACTION_REQUEST_START_ROUTINE
        }
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun setExactAlarmSafe(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun pauseSession(
        sessionId: String,
        sessionName: String,
        remainingSeconds: Long?,
        totalSeconds: Long?
    ) {
        val intent = Intent(
            context,
            SessionForegroundService::class.java
        ).apply {
            action = SessionForegroundService.ACTION_PAUSE_SESSION
            putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
            putExtra(SessionForegroundService.EXTRA_SESSION_NAME, sessionName)
            if (remainingSeconds != null) {
                putExtra(SessionForegroundService.EXTRA_REMAINING_SECONDS, remainingSeconds)
            }
            if (totalSeconds != null) {
                putExtra(SessionForegroundService.EXTRA_TOTAL_SECONDS, totalSeconds)
            }
        }
        context.startForegroundService(intent)
    }

    override fun resumeSession(
        sessionId: String,
        sessionName: String,
        remainingSeconds: Long?,
        totalSeconds: Long?
    ) {
        val intent = Intent(
            context,
            SessionForegroundService::class.java
        ).apply {
            action = SessionForegroundService.ACTION_RESUME_SESSION
            putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
            putExtra(SessionForegroundService.EXTRA_SESSION_NAME, sessionName)
            if (remainingSeconds != null) {
                putExtra(SessionForegroundService.EXTRA_REMAINING_SECONDS, remainingSeconds)
            }
            if (totalSeconds != null) {
                putExtra(SessionForegroundService.EXTRA_TOTAL_SECONDS, totalSeconds)
            }
        }
        context.startForegroundService(intent)
    }

    override fun createRoutinePendingIntent(
        routineId: String,
        configure: Intent.() -> Unit
    ): PendingIntent {
        val intent = Intent(
            context,
            SessionForegroundService::class.java
        ).apply(configure)

        return PendingIntent.getForegroundService(
            context,
            (routineId + (intent.action ?: "")).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun createSessionPendingIntent(
        sessionId: String,
        configure: Intent.() -> Unit
    ): PendingIntent {
        val intent = Intent(
            context,
            SessionForegroundService::class.java
        ).apply(configure)

        return PendingIntent.getForegroundService(
            context,
            (sessionId + (intent.action ?: "")).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun startSession(
        sessionId: String,
        sessionName: String,
        remainingSeconds: Long?,
        totalSeconds: Long?
    ) {
        val intent = Intent(context, SessionForegroundService::class.java).apply {
            putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
            putExtra(SessionForegroundService.EXTRA_SESSION_NAME, sessionName)
            if (remainingSeconds != null) {
                putExtra(SessionForegroundService.EXTRA_REMAINING_SECONDS, remainingSeconds)
            }
            if (totalSeconds != null) {
                putExtra(SessionForegroundService.EXTRA_TOTAL_SECONDS, totalSeconds)
            }
            action = SessionForegroundService.ACTION_START_SESSION
        }
        context.startForegroundService(intent)
    }

    override fun stopSession(sessionId: String) {
        val intent = Intent(context, SessionForegroundService::class.java).apply {
            action = SessionForegroundService.ACTION_STOP_SESSION
            putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
        }
        context.startForegroundService(intent)
    }

    override fun cancelScheduledSession(sessionId: String) {
        val pendingIntent = createSessionPendingIntent(sessionId) {
            putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
            action = SessionForegroundService.ACTION_REQUEST_STOP_SESSION
        }
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
