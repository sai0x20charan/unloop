package com.charan.unloop.data.repository.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.provider.Settings
import android.text.format.DateFormat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charan.unloop.core.di.ApplicationScope
import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.data.model.TimeFormatOptions
import com.charan.unloop.data.repository.DataStoreRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.shareIn

class DataStoreRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope
) : DataStoreRepository {
    companion object {
        private val Context.dataStore by preferencesDataStore("app_preferences")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_THEME = stringPreferencesKey("theme_option")
        private val KEY_TIME_FORMAT = stringPreferencesKey("time_format")
        private val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    }

    override val timeFormat: Flow<TimeFormatOptions> = context.dataStore.data.map { preferences ->
        val name = preferences[KEY_TIME_FORMAT]
        TimeFormatOptions.entries.firstOrNull { it.name == name } ?: TimeFormatOptions.SYSTEM_DEFAULT
    }

    override suspend fun setTimeFormat(format: TimeFormatOptions) {
        context.dataStore.edit { it[KEY_TIME_FORMAT] = format.name }
    }




    override val is24Hour: Flow<Boolean> = timeFormat
        .map { format ->
            when (format) {
                TimeFormatOptions.SYSTEM_DEFAULT -> DateFormat.is24HourFormat(context)
                TimeFormatOptions.TWELVE_HOUR -> false
                TimeFormatOptions.TWENTY_FOUR_HOUR -> true
            }
        }
        .distinctUntilChanged()
        .shareIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 5_000,
                replayExpirationMillis = 0
            ),
            replay = 1
        )


    override val isOnboardingCompleted: Flow<Boolean>
        get() = context.dataStore.data.map { pref ->
            pref[KEY_ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { pref ->
            pref[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setTheme(theme: ThemeOptions) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme.name
        }
    }

    override val theme: Flow<ThemeOptions>
        get() = context.dataStore.data.map { pref ->
            val themeName = pref[KEY_THEME] ?: ThemeOptions.SYSTEM_DEFAULT.name
            runCatching { ThemeOptions.valueOf(themeName) }.getOrDefault(ThemeOptions.SYSTEM_DEFAULT)
        }

    override suspend fun setDynamicColorsEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLORS] = isEnabled
        }
    }

    override val isDynamicColorsEnabled: Flow<Boolean>
        get() = context.dataStore.data.map { pref ->
            pref[KEY_DYNAMIC_COLORS] ?: true
        }
}
