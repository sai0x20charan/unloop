package com.charan.unloop.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import com.charan.unloop.MainActivity
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.session.SessionController
import com.charan.unloop.session.SessionForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionController: SessionController
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "session_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Session Progress"
        const val NOTIFICATION_ID = 1
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildForegroundNotification(
        title: String = context.getString(R.string.home_session_title_default),
        sessionId: String? = null,
        remainingSeconds: Long? = null,
        totalTime: Long? = null,
        isPaused: Boolean = false,
        areSessionControlsLocked: Boolean = false
    ): Notification {
        val title = "$title${if (isPaused) " - Paused" else ""}"
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val formatedDuration = DateTimeUtils.formatDuration(remainingSeconds?.toInt() ?: 0)

        val builder = Notification.Builder(
            context,
            NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle(title)
            .setSmallIcon(R.drawable.unloop_notification_icon)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentText(
                "Remaining Time: $formatedDuration"
            )
            .setShowWhen(true)
            .setContentIntent(contentIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)

        if (sessionId != null && !areSessionControlsLocked) {
            val pauseIntent = sessionController.createSessionPendingIntent(
                sessionId
            ) {
                putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
                action = SessionForegroundService.ACTION_REQUEST_PAUSE_SESSION
            }

            val resumeIntent = sessionController.createSessionPendingIntent(
                sessionId
            ) {
                putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
                action = SessionForegroundService.ACTION_REQUEST_RESUME_SESSION
            }

            val stopIntent = sessionController.createSessionPendingIntent(
                sessionId
            ) {
                putExtra(SessionForegroundService.EXTRA_SESSION_ID, sessionId)
                action = SessionForegroundService.ACTION_REQUEST_STOP_SESSION
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
                builder.setShortCriticalText(formatedDuration)


            } else {
                builder.setProgress(max, progress, false)
            }
        }

        return builder.build()
    }

    fun updateNotification(
        title: String = context.getString(R.string.home_session_title_default),
        message: String = "Monitoring app usage for routine",
        sessionId: String? = null,
        remainingSeconds: Long? = null,
        totalTime: Long? = null,
        isPaused: Boolean = false,
        areSessionControlsLocked: Boolean = false
    ): Notification {
        val notification = buildForegroundNotification(
            title = title,
            sessionId = sessionId,
            remainingSeconds = remainingSeconds,
            totalTime = totalTime,
            isPaused = isPaused,
            areSessionControlsLocked = areSessionControlsLocked
        )
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }
}
