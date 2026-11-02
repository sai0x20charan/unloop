package com.charan.unloop.session

import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.data.local.model.enums.UnlockChallengeType

interface SessionManager {

    suspend fun startInstantSession(
        name: String,
        durationSeconds: Long,
        blockedAppPackages: List<String>,
        areSessionControlsLocked : Boolean,
        unlockChallengeType: UnlockChallengeType,
        waitDurationSeconds : Int,
    ): String?

    suspend fun stopSession(sessionId : String)

    suspend fun pauseSession(sessionId : String)

    suspend fun resumeSession(sessionId : String)

    suspend fun logSessionEvent(sessionId : String, eventName : SessionEventType, packageName: String?)

    suspend fun scheduleNextRoutineStart(routineId : String)

    suspend fun scheduleEnd(sessionId : String)

    suspend fun cancelSessionEndAlarm(sessionId : String)

    suspend fun startScheduledSession(routineId: String): String?

    suspend fun cancelScheduledRoutine(routineId: String)

    suspend fun restoreSession(sessionId : String)

}
