package com.charan.unloop.core.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.charan.unloop.service.FullScreenOverlayService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoutineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val routineId = intent.getStringExtra("routineId")
            if (routineId != null) {
                Log.d("TAG", "onReceive: RoutineReceiver received routineId: $routineId")
                val serviceIntent = Intent(context, RoutineForegroundService::class.java)
                serviceIntent.putExtra("routineId", routineId)
                context.startForegroundService(serviceIntent)
            }
        }

    }
}