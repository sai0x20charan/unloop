package com.charan.stepaway.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.charan.stepaway.data.local.entity.RoutineEntity

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutine(routineEntity: RoutineEntity)

    @Query("SELECT * FROM ROUTINES WHERE id = :routineId")
    suspend fun getRoutineById(routineId: String): RoutineEntity?

}