package com.charan.unloop.session

import android.app.PendingIntent
import android.content.Intent
import kotlinx.datetime.LocalDateTime

interface SessionController {

    fun scheduleRoutine(routineId: String, scheduleTime: LocalDateTime)

    fun scheduleStopSession(sessionId: String, endTime: LocalDateTime)

    fun cancelRoutine(routineId: String)

    fun pauseSession(
        sessionId: String,
        sessionName: String,
        remainingSeconds: Long? = null,
        totalSeconds: Long? = null
    )

    fun resumeSession(
        sessionId: String,
        sessionName: String,
        remainingSeconds: Long? = null,
        totalSeconds: Long? = null
    )

    fun createRoutinePendingIntent(
        routineId: String,
        configure: Intent.() -> Unit
    ): PendingIntent

    fun createSessionPendingIntent(
        sessionId: String,
        configure: Intent.() -> Unit
    ): PendingIntent

    fun startSession(
        sessionId: String,
        sessionName: String,
        remainingSeconds: Long? = null,
        totalSeconds: Long? = null
    )

    fun stopSession(sessionId: String)

    fun cancelScheduledSession(sessionId: String)
}
