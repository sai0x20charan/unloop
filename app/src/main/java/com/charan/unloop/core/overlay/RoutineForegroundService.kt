package com.charan.unloop.core.overlay

import android.app.AlarmManager
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.charan.unloop.R
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.service.FullScreenOverlayService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class RoutineForegroundService : Service() {

    @Inject
    lateinit var appUsageChecker: AppUsageChecker

    @Inject
    lateinit var routineRepository: RoutineRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var routineScheduler: RoutineScheduler

    private val alarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    companion object {
        const val ACTION_STOP_ROUTINE = "com.charan.unloop.ACTION_STOP_ROUTINE"
        const val EXTRA_ROUTINE_ID = "routineId"
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

        val routineId = intent?.getStringExtra(EXTRA_ROUTINE_ID)
        val action = intent?.action

        if (action == ACTION_STOP_ROUTINE) {
            stopRoutine(routineId)
            return START_NOT_STICKY
        }

        notificationHelper.createNotificationChannel()

        val notification = Notification.Builder(this, NotificationHelper.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Routine Active")
            .setContentText("Monitoring app usage for routine")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .build()

        startForeground(NotificationHelper.NOTIFICATION_ID, notification)

        startMonitoring(routineId)

        return START_STICKY
    }

    fun stopRoutine(routineId: String?) {
        if (routineId != null) {
            routineScheduler.stopAlarm(routineId)
        }
        val overlayIntent = Intent(this, FullScreenOverlayService::class.java)
        stopService(overlayIntent)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }


    private fun startMonitoring(routineId: String?) {
        serviceScope.launch {
            if (routineId != null) {
                val routine = routineRepository.getRoutineById(routineId)
                if (routine != null && routine.endTime != null && !routine.isAllDay) {
                    routineScheduler.scheduleStopRoutine(routineId, routine.endTime)
                }
            }

            while (true) {

                val foregroundPackage =
                    appUsageChecker.getForegroundApp()

                if (foregroundPackage != null) {
                    val routine = routineId?.let { routineRepository.getRoutineById(it) }

                    val blockedAppPackages = routine?.disableAppPackages
                    if(blockedAppPackages?.contains(foregroundPackage) == true){

                        val overlayIntent = Intent(this@RoutineForegroundService, FullScreenOverlayService::class.java)
                        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startService(overlayIntent)
                    } else {
                        val overlayIntent = Intent(this@RoutineForegroundService, FullScreenOverlayService::class.java)
                        stopService(overlayIntent)


                    }

                    Log.d("TAG", "startMonitoring: hi $foregroundPackage")

                    // check if package is blocked
                    // show overlay if needed
                }

                delay(1000.milliseconds)
            }
        }
    }


    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
