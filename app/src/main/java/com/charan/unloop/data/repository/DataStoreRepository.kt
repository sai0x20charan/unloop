package com.charan.unloop.data.repository

import com.charan.unloop.data.model.ThemeOptions
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setTheme(theme: ThemeOptions)

    val theme: Flow<ThemeOptions>

    suspend fun setDynamicColorsEnabled(isEnabled: Boolean)

    val isDynamicColorsEnabled: Flow<Boolean>
}
