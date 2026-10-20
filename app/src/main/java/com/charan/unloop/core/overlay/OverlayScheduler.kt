package com.charan.unloop.core.overlay

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.charan.unloop.core.utils.DateUtils
import com.charan.unloop.data.local.model.enums.SessionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {

    fun scheduleRoutine(
        routineId: String,
        startTime: LocalTime?,
        frequency: List<DayOfWeek>,
        isAllDay: Boolean = false
    ) {
        if (startTime == null || frequency.isEmpty()) return

        val now = DateUtils.getCurrentDateTime()
        val pendingIntent = createPendingIntent(routineId)

        var date = now.date

        for (i in 0..7) {
            val dateTime = LocalDateTime(
                year = date.year,
                month = date.month,
                day = date.day,
                hour = startTime.hour,
                minute = startTime.minute,
                second = startTime.second,
                nanosecond = 0
            )

            if (frequency.contains(dateTime.dayOfWeek) && (isAllDay || dateTime > now)) {
                val millis = dateTime
                    .toInstant(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()

                setExactAlarmSafe(millis, pendingIntent)
                return
            }

            date = date.plus(1, DateTimeUnit.DAY)
        }
    }

    fun cancelRoutine(routineId: String) {
        val pendingIntent = createPendingIntent(routineId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        cancelStopRoutine(routineId)

        context.stopService(Intent(context, RoutineForegroundService::class.java))
    }

    fun scheduleStopRoutine(
        sessionId: String,
        endTime: LocalDateTime
    ) {
        if (sessionId.isBlank()) return

        val now = DateUtils.getCurrentDateTime()
        if (endTime <= now) {
            val stopIntent = Intent(context, RoutineForegroundService::class.java).apply {
                action = RoutineForegroundService.ACTION_STOP_ROUTINE
                putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
            }
            context.startForegroundService(stopIntent)
            return
        }

        val millis = endTime
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()

        setExactAlarmSafe(millis, createStopPendingIntent(sessionId))
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

    fun pauseRoutine(sessionId: String) {
        val intent = Intent(
            context,
            RoutineForegroundService::class.java
        ).apply {
            action = RoutineForegroundService.ACTION_PAUSE_ROUTINE
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
        }

        context.startService(intent)
    }

    fun resumeRoutine(sessionId: String) {
        val intent = Intent(
            context,
            RoutineForegroundService::class.java
        ).apply {
            action = RoutineForegroundService.ACTION_RESUME_ROUTINE
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
        }

        context.startForegroundService(intent)
    }

    fun cancelStopRoutine(sessionId: String) {
        if (sessionId.isBlank()) return
        val pendingIntent = createStopPendingIntent(sessionId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun createPendingIntent(routineId: String): PendingIntent {
        val intent = Intent(context, RoutineForegroundService::class.java).apply {
            putExtra(RoutineForegroundService.EXTRA_ROUTINE_ID, routineId)
        }

        return PendingIntent.getForegroundService(
            context,
            routineId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createStopPendingIntent(sessionId: String): PendingIntent {
        val intent = Intent(context, RoutineForegroundService::class.java).apply {
            action = RoutineForegroundService.ACTION_STOP_ROUTINE
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
        }

        return PendingIntent.getForegroundService(
            context,
            (sessionId + "_stop").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun startSession(sessionId: String) {
        val intent = Intent(context, RoutineForegroundService::class.java).apply {
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
        }
        context.startForegroundService(intent)
    }

    fun stopSession(sessionId: String) {
        cancelStopRoutine(sessionId)
        val intent = Intent(context, RoutineForegroundService::class.java).apply {
            action = RoutineForegroundService.ACTION_STOP_ROUTINE
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
        }
        context.startForegroundService(intent)
    }
}
