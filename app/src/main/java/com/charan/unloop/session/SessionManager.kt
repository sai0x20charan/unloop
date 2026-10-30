package com.charan.unloop.session

import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.presentation.routine.UnlockChallengeType

interface SessionManager {

    suspend fun startInstantSession(
        name: String,
        durationSeconds: Long,
        blockedApps: List<String>,
        areSessionControlsLocked : Boolean,
        unlockChallengeType: UnlockChallengeType,
        waitTime : Int,
    ): String?

    suspend fun stopSession(sessionId : String)

    suspend fun pauseSession(sessionId : String)

    suspend fun resumeSession(sessionId : String)

    suspend fun logSessionEvent(sessionId : String, eventName : SessionEvents, appPackage: String?)

    suspend fun scheduleSession(routineId : String)

    suspend fun scheduleEnd(sessionId : String)

    suspend fun cancelScheduledSession(sessionId : String)

    suspend fun startScheduledSession(routineId: String): String?

    suspend fun cancelScheduledRoutine(routineId: String)

    suspend fun restoreSession(sessionId : String)

}
