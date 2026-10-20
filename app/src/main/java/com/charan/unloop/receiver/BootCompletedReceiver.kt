package com.charan.unloop.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.charan.unloop.core.overlay.RoutineScheduler
import com.charan.unloop.data.repository.RoutineRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var routineRepository: RoutineRepository

    @Inject
    lateinit var routineScheduler: RoutineScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            scope.launch {
                try {
                    val routines = routineRepository.getAllRoutines().firstOrNull().orEmpty()
                    val activeRoutines = routines.filter { it.isEnabled && !it.isDeleted }

                    for (routine in activeRoutines) {
                        if (!routine.daysOfWeek.isNullOrEmpty() && routine.startTime != null) {
                            routineScheduler.scheduleRoutine(
                                routineId = routine.id,
                                startTime = routine.startTime,
                                frequency = routine.daysOfWeek,
                                isAllDay = routine.isAllDay
                            )
                        }
                    }
                    Log.d(TAG, "Successfully rescheduled ${activeRoutines.size} routines after reboot")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule routines on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
