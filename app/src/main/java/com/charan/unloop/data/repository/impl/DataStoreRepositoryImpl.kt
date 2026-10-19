package com.charan.unloop.data.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.charan.unloop.data.repository.DataStoreRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    @ApplicationContext private val context : Context
) : DataStoreRepository {
    companion object {

        private val Context.dataStore by preferencesDataStore("app_preferences")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }


    override val isOnBoardingCompleted: Flow<Boolean>
        get() = context.dataStore.data.map { pref ->
            val isCompleted = pref[KEY_ONBOARDING_COMPLETED] ?: false
            isCompleted
        }

    override suspend fun setOnBoardingCompleted(completed: Boolean) {
        context.dataStore.edit { pref ->
            pref[KEY_ONBOARDING_COMPLETED] = completed
        }

    }


}