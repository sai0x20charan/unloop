package com.charan.stepaway.data.repository

import com.charan.stepaway.data.local.entity.RoutineEntity

interface RoutineRepository {
    suspend fun upsertRoutine(routineEntity: RoutineEntity)

    suspend fun getRoutineById(routineId: String): RoutineEntity?
}