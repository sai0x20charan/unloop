package com.charan.stepaway.data.repository.impl

import com.charan.stepaway.data.local.dao.RoutineDao
import com.charan.stepaway.data.local.entity.RoutineEntity
import com.charan.stepaway.data.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {
    override suspend fun upsertRoutine(routineEntity: RoutineEntity) {
        withContext(Dispatchers.IO){
            routineDao.upsertRoutine(routineEntity)

        }
    }

    override suspend fun getRoutineById(routineId: String): RoutineEntity? {
        return withContext(Dispatchers.IO) {
            routineDao.getRoutineById(routineId)
        }
    }
}