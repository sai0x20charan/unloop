package com.charan.unloop.data.repository

import com.charan.unloop.data.local.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    suspend fun upsertRoutine(routineEntity: RoutineEntity)

    suspend fun upsertRoutine(routineEntity: List<RoutineEntity>)

    suspend fun getRoutineById(routineId: String): RoutineEntity?

    fun observeAllRoutines(): Flow<List<RoutineEntity>>

    suspend fun getAllRoutines() : List<RoutineEntity>

    suspend fun updateRoutineEnabled(routineId: String, isEnabled: Boolean)

    suspend fun deleteRoutine(routineId: String)
}
