package com.charan.unloop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.charan.unloop.data.local.entity.SessionEventEntity

@Dao
interface SessionEventDao {
    @Insert
    suspend fun insertSessionEvent(sessionEvents: SessionEventEntity)
}
