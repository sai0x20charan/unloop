package com.charan.stepaway.core.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.charan.stepaway.service.FullScreenOverlayService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoutineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val routineId = intent.getStringExtra("routineId")
            if (routineId != null) {
                val serviceIntent = Intent(context, RoutineForegroundService::class.java)
                serviceIntent.putExtra("routineId", routineId)
                context.startForegroundService(serviceIntent)
            }
        }

    }
}