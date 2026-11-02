package com.charan.unloop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.SessionWithSessionEvents
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("""
    SELECT * FROM sessions 
    WHERE status IN (:statuses)
    LIMIT 1
""")
    fun getActiveSession(
        statuses: List<SessionStatus> = listOf(
            SessionStatus.RUNNING,
            SessionStatus.PAUSED
        )
    ): Flow<SessionEntity?>

    @Query("""
    SELECT * FROM sessions 
    WHERE status IN (:statuses)
    LIMIT 1
""")
    suspend fun getActiveSessionOnce(
        statuses: List<SessionStatus> = listOf(
            SessionStatus.RUNNING,
            SessionStatus.PAUSED
        )
    ): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    @Query("""
    UPDATE sessions 
    SET status = :status, actualEndTime = :actualEndTime
    WHERE id = :sessionId
""")
    suspend fun updateSessionStatus(
        sessionId: String,
        status: SessionStatus,
        actualEndTime: LocalDateTime?
    )

    @Query("SELECT * from sessions WHERE type = :sessionType ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastInstantSession(sessionType: SessionType = SessionType.INSTANT): SessionEntity?

    @Query("SELECT * from sessions")
    suspend fun getAllSessions(): List<SessionEntity>
    @Transaction
    @Query("SELECT * from sessions WHERE createdAt BETWEEN :startDate AND :endTime")
    suspend fun getSessionForRange(
        startDate: LocalDateTime,
        endTime: LocalDateTime
    ): List<SessionWithSessionEvents>

    @Transaction
    @Query("SELECT * from sessions WHERE id = :sessionId")
    suspend fun getSessionWithSessionEventsById(sessionId: String): SessionWithSessionEvents

}
