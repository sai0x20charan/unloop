package com.charan.unloop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.charan.unloop.data.local.entity.SessionEventEntity

@Dao
interface SessionEventDao {
    @Insert
    suspend fun insertSessionEvent(sessionEvents: SessionEventEntity)

    @Upsert
    suspend fun insertSessionEvents(sessionEvents: List<SessionEventEntity>)

    @Query("DELETE FROM session_events WHERE sessionId IN (:sessionIds)")
    suspend fun deleteEventsForSessions(sessionIds: List<String>)

    @Query("SELECT * FROM session_events WHERE sessionId = :sessionId")
    suspend fun getEventsForSession(sessionId: String): List<SessionEventEntity>
}
