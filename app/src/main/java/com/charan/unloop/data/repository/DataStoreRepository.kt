package com.charan.unloop.data.repository

import com.charan.unloop.data.model.ThemeOptions
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val isOnBoardingCompleted: Flow<Boolean>

    suspend fun setOnBoardingCompleted(completed: Boolean)

    suspend fun setTheme(theme: ThemeOptions)

    val getTheme: Flow<ThemeOptions>

    suspend fun setDynamicColorsState(isEnabled: Boolean)

    val getDynamicColorsState: Flow<Boolean>
}
