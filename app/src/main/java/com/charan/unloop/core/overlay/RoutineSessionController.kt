package com.charan.unloop.core.overlay

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineSessionController @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleRoutine(
        routineId: String,
        scheduleTime: LocalDateTime,
    ) {

        val pendingIntent = createRoutinePendingIntent(routineId) {
            putExtra(RoutineForegroundService.EXTRA_ROUTINE_ID, routineId)
            action = RoutineForegroundService.ACTION_REQUEST_START_ROUTINE
        }
        setExactAlarmSafe(
            scheduleTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            pendingIntent
        )

    }

    fun scheduleStopSession(
        sessionId: String,
        endTime: LocalDateTime
    ) {
        if (sessionId.isBlank()) return
        val pendingIntent = createSessionPendingIntent(sessionId){
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
            action = RoutineForegroundService.ACTION_REQUEST_STOP_SESSION
        }
        setExactAlarmSafe(
            endTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            pendingIntent
        )
    }

    fun cancelRoutine(routineId: String) {
        val pendingIntent = createRoutinePendingIntent(routineId){
            putExtra(RoutineForegroundService.EXTRA_ROUTINE_ID, routineId)
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

    fun pauseSession(sessionId: String) {
        val intent = Intent(
            context,
            RoutineForegroundService::class.java
        ).apply {
            action = RoutineForegroundService.ACTION_PAUSE_SESSION
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
        }
        context.startForegroundService(intent)
    }


    fun resumeRoutine(sessionId: String) {
        val intent = Intent(
            context,
            RoutineForegroundService::class.java
        ).apply {
            action = RoutineForegroundService.ACTION_RESUME_SESSION
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
        }

        context.startForegroundService(intent)
    }

    private fun createRoutinePendingIntent(
        routineId: String,
        configure: Intent.() -> Unit
    ): PendingIntent {
        val intent = Intent(
            context,
            RoutineForegroundService::class.java
        ).apply(configure)

        return PendingIntent.getForegroundService(
            context,
            routineId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSessionPendingIntent(
        sessionId : String,
        configure: Intent.() -> Unit
    ) : PendingIntent{
        val intent = Intent(
            context,
            RoutineForegroundService::class.java
        ).apply(configure)

        return PendingIntent.getForegroundService(
            context,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    fun startSession(sessionId: String) {
        val intent = Intent(context, RoutineForegroundService::class.java).apply {
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
            action = RoutineForegroundService.ACTION_START_SESSION
        }
        context.startForegroundService(intent)
    }

    fun stopSession(sessionId: String) {
        val intent = Intent(context, RoutineForegroundService::class.java).apply {
            action = RoutineForegroundService.ACTION_STOP_SESSION
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
        }
        context.startForegroundService(intent)
    }

    fun cancelScheduledSession(sessionId: String) {
        val pendingIntent = createSessionPendingIntent(sessionId){
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
            action = RoutineForegroundService.ACTION_REQUEST_STOP_SESSION
        }
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
