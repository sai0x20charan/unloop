package com.charan.unloop.data.repository.impl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.data.repository.DataStoreRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DataStoreRepository {
    companion object {
        private val Context.dataStore by preferencesDataStore("app_preferences")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_THEME = stringPreferencesKey("theme_option")
        private val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    }

    override val isOnBoardingCompleted: Flow<Boolean>
        get() = context.dataStore.data.map { pref ->
            pref[KEY_ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setOnBoardingCompleted(completed: Boolean) {
        context.dataStore.edit { pref ->
            pref[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setTheme(theme: ThemeOptions) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme.name
        }
    }

    override val getTheme: Flow<ThemeOptions>
        get() = context.dataStore.data.map { pref ->
            val themeName = pref[KEY_THEME] ?: ThemeOptions.SYSTEM_DEFAULT.name
            runCatching { ThemeOptions.valueOf(themeName) }.getOrDefault(ThemeOptions.SYSTEM_DEFAULT)
        }

    override suspend fun setDynamicColorsState(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLORS] = isEnabled
        }
    }

    override val getDynamicColorsState: Flow<Boolean>
        get() = context.dataStore.data.map { pref ->
            pref[KEY_DYNAMIC_COLORS] ?: true
        }
}
