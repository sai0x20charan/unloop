package com.charan.stepaway.core.overlay

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.charan.stepaway.core.utils.DateUtils
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
        if(startTime == null) return
        if (frequency.isEmpty()) return

        val now = DateUtils.currentDateTime()
        val triggerTime = startTime

        val pendingIntent = createPendingIntent(routineId)

        var date = now.date

        while (true) {

            val dateTime = LocalDateTime(
                year = date.year,
                month = date.month.number,
                day = date.day,
                hour = triggerTime.hour,
                minute = triggerTime.minute,
                second = 0,
                nanosecond = 0
            )

            val validDay = frequency.contains(dateTime.dayOfWeek)
            val futureTime = dateTime > now

            if (validDay && (isAllDay || futureTime)) {

                val millis = dateTime
                    .toInstant(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    millis,
                    pendingIntent
                )

                return
            }

            date = date.plus(1, DateTimeUnit.DAY)
        }
    }

    fun stopAlarm(routineId: String) {
        val pendingIntent = createPendingIntent(routineId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        cancelStopRoutine(routineId)

        val serviceIntent = Intent(context, RoutineForegroundService::class.java)
        context.stopService(serviceIntent)
    }

    fun scheduleStopRoutine(
        routineId: String,
        endTime: LocalTime
    ) {
        val now = DateUtils.currentDateTime()
        var endDateTime = LocalDateTime(
            year = now.year,
            month = now.month.number,
            day = now.day,
            hour = endTime.hour,
            minute = endTime.minute,
            second = 0,
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
                second = 0,
                nanosecond = 0
            )
        }

        val millis = endDateTime
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()

        val pendingIntent = createStopPendingIntent(routineId)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            millis,
            pendingIntent
        )
    }

    fun cancelStopRoutine(routineId: String) {
        val pendingIntent = createStopPendingIntent(routineId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun cancelAlarm(routineId: String) {
        stopAlarm(routineId)
    }

    fun stopRoutine(routineId: String) {
        stopAlarm(routineId)
    }

    fun cancelRoutine(routineId: String) {
        stopAlarm(routineId)
    }

    private fun createPendingIntent(routineId: String): PendingIntent {
        val intent = Intent(context, RoutineReceiver::class.java).apply {
            putExtra("routineId", routineId)
        }

        return PendingIntent.getBroadcast(
            context,
            routineId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createStopPendingIntent(routineId: String): PendingIntent {
        val intent = Intent(context, RoutineForegroundService::class.java).apply {
            action = RoutineForegroundService.ACTION_STOP_ROUTINE
            putExtra(RoutineForegroundService.EXTRA_ROUTINE_ID, routineId)
        }

        return PendingIntent.getForegroundService(
            context,
            (routineId + "_stop").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}