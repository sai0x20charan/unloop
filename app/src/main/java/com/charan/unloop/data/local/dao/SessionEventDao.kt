package com.charan.unloop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.charan.unloop.data.local.entity.SessionEventEntity

@Dao
interface SessionEventDao {
    @Insert
    suspend fun insertSessionEvent(sessionEvents: SessionEventEntity)

    @Query("SELECT * FROM session_events WHERE sessionId = :sessionId")
    suspend fun getEventsForSession(sessionId: String): List<SessionEventEntity>
}
