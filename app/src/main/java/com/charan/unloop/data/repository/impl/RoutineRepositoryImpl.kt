package com.charan.unloop.data.repository.impl

import com.charan.unloop.data.local.dao.RoutineDao
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.data.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {
    override suspend fun upsertRoutine(routineEntity: RoutineEntity) =
        routineDao.upsertRoutine(routineEntity)

    override suspend fun upsertRoutine(routineEntity: List<RoutineEntity>) {
        return routineDao.upsertRoutine(routineEntity)
    }

    override suspend fun getRoutineById(routineId: String): RoutineEntity? =
        routineDao.getRoutineById(routineId)

    override fun observeAllRoutines(): Flow<List<RoutineEntity>> =
        routineDao.getAllRoutines()

    override suspend fun updateRoutineEnabled(routineId: String, isEnabled: Boolean) =
        routineDao.updateRoutineEnabled(routineId, isEnabled)

    override suspend fun deleteRoutine(routineId: String) =
        routineDao.deleteRoutine(routineId)

    override suspend fun getAllRoutines(): List<RoutineEntity> {
        return routineDao.getAllRoutinesSnapshot()
    }
}
