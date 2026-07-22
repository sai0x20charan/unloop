package com.charan.stepaway.core.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.charan.stepaway.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {


    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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



    fun showRoutineProgressNotification(title : String,message : String,routineId: String) {
        val builder =
            Notification.Builder(context, NOTIFICATION_CHANNEL_ID)

        val notification = builder
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}