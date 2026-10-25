package com.charan.unloop.core.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.charan.unloop.MainActivity
import com.charan.unloop.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routineSessionController: RoutineSessionController
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "routine_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Routine Progress"
        const val NOTIFICATION_ID = 1
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun buildForegroundNotification(
        title: String = "Routine Active",
        message: String = "Monitoring app usage for routine",
        sessionId: String? = null,
        remainingSeconds: Long? = null,
        totalTime: Long? = null,
        isPaused: Boolean = false
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayTitle = if (isPaused && title == "Routine Active") "Routine Paused" else title

        val builder = Notification.Builder(
            context,
            NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle(displayTitle)
            .setContentText(message)
            .setSmallIcon(R.drawable.unloop_notification_icon)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)

        if (remainingSeconds != null) {
            if (!isPaused) {
                builder
                    .setWhen(
                        System.currentTimeMillis() +
                                remainingSeconds * 1000L
                    )
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
            } else {
                val hours = remainingSeconds / 3600
                val minutes = (remainingSeconds % 3600) / 60
                val seconds = remainingSeconds % 60

                builder
                    .setUsesChronometer(false)
                    .setShowWhen(false)
                    .setContentText(
                        "Paused • %02d:%02d:%02d remaining".format(
                            hours, minutes, seconds
                        )
                    )
            }
        } else {
            builder.setShowWhen(false)
            if (isPaused) {
                builder.setContentText("Routine Paused")
            }
        }

        if (sessionId != null) {
            val pauseIntent = routineSessionController.createSessionPendingIntent(
                sessionId
            ) {
                putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
                action = RoutineForegroundService.ACTION_REQUEST_PAUSE_SESSION
            }

            val resumeIntent = routineSessionController.createSessionPendingIntent(
                sessionId
            ) {
                putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
                action = RoutineForegroundService.ACTION_REQUEST_RESUME_SESSION
            }

            val stopIntent = routineSessionController.createSessionPendingIntent(
                sessionId
            ) {
                putExtra(RoutineForegroundService.EXTRA_SESSION_ID, sessionId)
                action = RoutineForegroundService.ACTION_REQUEST_STOP_SESSION
            }

            if (isPaused) {
                builder.addAction(
                    Notification.Action.Builder(
                        null,
                        "Resume",
                        resumeIntent
                    ).build()
                )
            } else {
                builder.addAction(
                    Notification.Action.Builder(
                        null,
                        "Pause",
                        pauseIntent
                    ).build()
                )
            }

            builder.addAction(
                Notification.Action.Builder(
                    null,
                    "Stop",
                    stopIntent
                ).build()
            )
        }

        if (totalTime != null && remainingSeconds != null) {
            val max = totalTime.coerceAtLeast(1L).toInt()
            val progress = (totalTime - remainingSeconds).coerceIn(0L, totalTime).toInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                val progressStyle = Notification.ProgressStyle()
                    .addProgressSegment(Notification.ProgressStyle.Segment(max))
                    .setProgress(progress)
                builder.setStyle(progressStyle)
                builder.setRequestPromotedOngoing(true)
            } else {
                builder.setProgress(max, progress, false)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                builder.setRequestPromotedOngoing(true)
            } else {
                builder.setProgress(0, 0, false)
            }
        }

        return builder.build()
    }

    fun updateNotification(
        title: String = "Routine Active",
        message: String = "Monitoring app usage for routine",
        sessionId: String? = null,
        remainingSeconds: Long? = null,
        totalTime: Long? = null,
        isPaused: Boolean = false
    ): Notification {
        val notification = buildForegroundNotification(
            title = title,
            message = message,
            sessionId = sessionId,
            remainingSeconds = remainingSeconds,
            totalTime = totalTime,
            isPaused = isPaused
        )
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }
}
