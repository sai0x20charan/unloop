package com.charan.unloop.data.repository

import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.data.model.TimeFormatOptions
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val timeFormat: Flow<TimeFormatOptions>
    val is24Hour: Flow<Boolean>

    suspend fun setTimeFormat(format: TimeFormatOptions)

    val isOnboardingCompleted: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setTheme(theme: ThemeOptions)

    val theme: Flow<ThemeOptions>

    suspend fun setDynamicColorsEnabled(isEnabled: Boolean)

    val isDynamicColorsEnabled: Flow<Boolean>
}
