package com.charan.unloop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity)

    @Query("""
    SELECT * FROM sessions 
    WHERE sessionStatus IN (:statuses)
    LIMIT 1
""")
    fun getActiveSession(
        statuses: List<SessionStatus> = listOf(
            SessionStatus.RUNNING,
            SessionStatus.PAUSED
        )
    ): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): SessionEntity?
}