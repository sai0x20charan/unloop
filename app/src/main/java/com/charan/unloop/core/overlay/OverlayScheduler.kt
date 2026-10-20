package com.charan.unloop.core.overlay

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.charan.unloop.core.utils.DateUtils
import com.charan.unloop.service.FullScreenOverlayService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import javax.inject.Inject

class RoutineScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleRoutine(
        routineId: String,
        startTime: LocalTime?,
        frequency: List<DayOfWeek>,
        isAllDay: Boolean = false
    ) {
        if (startTime == null || frequency.isEmpty()) return

        val now = DateUtils.currentDateTime()
        val pendingIntent = createPendingIntent(routineId)
        var date = now.date

        while (true) {
            val dateTime = LocalDateTime(
                year = date.year,
                month = date.month.number,
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
        endTime: LocalTime
    ) {
        if (sessionId.isBlank()) return

        val now = DateUtils.currentDateTime()
        var endDateTime = LocalDateTime(
            year = now.year,
            month = now.month.number,
            day = now.day,
            hour = endTime.hour,
            minute = endTime.minute,
            second = endTime.second,
            nanosecond = 0
        )

        if (endDateTime <= now) {
            val nextDate = now.date.plus(1, DateTimeUnit.DAY)
            endDateTime = LocalDateTime(
                year = nextDate.year,
                month = nextDate.month.number,
                day = nextDate.day,
                hour = endTime.hour,
                minute = endTime.minute,
                second = endTime.second,
                nanosecond = 0
            )
        }

        val millis = endDateTime
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

    fun pauseRoutine(sessionId: String, remainingSeconds: Int? = null) {
        val intent = Intent(
            context,
            RoutineForegroundService::class.java
        ).apply {
            action = RoutineForegroundService.ACTION_PAUSE_ROUTINE
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
            if (remainingSeconds != null) {
                putExtra(RoutineForegroundService.EXTRA_REMAINING_SECONDS, remainingSeconds)
            }
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

    fun startSession(id: String) {
        val serviceIntent = Intent(context, RoutineForegroundService::class.java).apply {
            putExtra(RoutineForegroundService.EXTRA_SESSION_ID, id)
        }
        context.startForegroundService(serviceIntent)
    }

    fun stopSession(sessionId: String) {
        cancelStopRoutine(sessionId)
        context.stopService(Intent(context, FullScreenOverlayService::class.java))
        context.stopService(Intent(context, RoutineForegroundService::class.java))
    }
}
