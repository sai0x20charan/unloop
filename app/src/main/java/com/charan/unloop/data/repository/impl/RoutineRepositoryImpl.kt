package com.charan.unloop.data.repository.impl

import com.charan.unloop.data.local.dao.RoutineDao
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.data.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {
    override suspend fun upsertRoutine(routineEntity: RoutineEntity) {
        withContext(Dispatchers.IO) {
            routineDao.upsertRoutine(routineEntity)
        }
    }

    override suspend fun getRoutineById(routineId: String): RoutineEntity? {
        return withContext(Dispatchers.IO) {
            routineDao.getRoutineById(routineId)
        }
    }

    override fun getAllRoutines(): Flow<List<RoutineEntity>> {
        return routineDao.getAllRoutines().flowOn(Dispatchers.IO)
    }

    override suspend fun updateRoutineEnabled(routineId: String, isEnabled: Boolean) {
        withContext(Dispatchers.IO) {
            routineDao.updateRoutineEnabled(routineId, isEnabled)
        }
    }

    override suspend fun deleteRoutine(routineId: String) {
        withContext(Dispatchers.IO) {
            routineDao.deleteRoutine(routineId)
        }
    }
}
