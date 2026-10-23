package com.charan.unloop.data.repository

import com.charan.unloop.data.local.model.enums.SessionEvents

interface SessionManagerRepository  {

    suspend fun startSession(sessionId : String)

    suspend fun stopSession(sessionId : String)

    suspend fun pauseSession(sessionId : String)

    suspend fun resumeSession(sessionId : String)

    suspend fun logSessionEvent(sessionId : String, eventName : SessionEvents, appPackage: String?)

    suspend fun scheduleSession(routineId : String)

    suspend fun scheduleEnd(sessionId : String)

    suspend fun cancelScheduledSession(sessionId : String)

    suspend fun createSession(routineId: String,

    ): String?

    suspend fun cancelScheduledRoutine(routineId: String)

    suspend fun replaceSession(sessionId : String)

}