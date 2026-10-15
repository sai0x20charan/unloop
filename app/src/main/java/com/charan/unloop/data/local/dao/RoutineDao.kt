package com.charan.unloop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.charan.unloop.data.local.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutine(routineEntity: RoutineEntity)

    @Query("SELECT * FROM ROUTINES WHERE id = :routineId")
    suspend fun getRoutineById(routineId: String): RoutineEntity?

    @Query("SELECT * FROM routines WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("UPDATE routines SET isEnabled = :isEnabled WHERE id = :routineId")
    suspend fun updateRoutineEnabled(routineId: String, isEnabled: Boolean)

    @Query("UPDATE routines SET isDeleted = 1 WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: String)
}
