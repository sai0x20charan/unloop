package com.charan.unloop.data.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val isOnBoardingCompleted : Flow<Boolean>

    suspend fun setOnBoardingCompleted(completed : Boolean)
}