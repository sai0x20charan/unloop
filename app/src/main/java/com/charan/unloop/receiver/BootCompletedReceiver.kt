package com.charan.unloop.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.charan.unloop.core.utils.PermissionManager
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.repository.SessionManagerRepository
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.data.repository.SessionRepository
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
    lateinit var sessionManager: SessionManagerRepository

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val isAlarmPermissionGrant = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED

        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            isAlarmPermissionGrant
        ) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            scope.launch {
                try {
                    if (isAlarmPermissionGrant && !permissionManager.hasExactAlarmPermission()) return@launch
                    val runningSession = sessionRepository.getActiveSessionOnce()
                    if(runningSession !=null && runningSession.sessionStatus == SessionStatus.RUNNING){
                        if (isAlarmPermissionGrant) {
                            sessionManager.scheduleEnd(runningSession.id)
                        } else {
                            sessionManager.replaceSession(runningSession.id)
                        }
                    }
                    val routines = routineRepository.getAllRoutines().firstOrNull().orEmpty()
                    val activeRoutines = routines.filter { it.isEnabled && !it.isDeleted }

                    for (routine in activeRoutines) {
                        sessionManager.scheduleSession(routine.id)
                    }
                    Log.d(TAG, "Rescheduled ${activeRoutines.size} routines after ${intent.action}")
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
