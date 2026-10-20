package com.charan.unloop.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observeSettingsDataStore()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnThemeChange -> {
                changeTheme(event.theme)
            }
            is SettingsEvent.OnDynamicColorsChange -> {
                setDynamicColorsState(event.isEnabled)
            }
            SettingsEvent.OnBack -> {
                sendEffect(SettingsEffect.OnBack)
            }
        }
    }

    private fun observeSettingsDataStore() = viewModelScope.launch {
        combine(
            dataStore.getTheme,
            dataStore.getDynamicColorsState
        ) { theme, dynamic ->
            theme to dynamic
        }.collectLatest { (theme, dynamic) ->
            _state.update {
                it.copy(
                    theme = theme,
                    isDynamicColorsEnabled = dynamic
                )
            }
        }
    }

    private fun changeTheme(theme: ThemeOptions) = viewModelScope.launch {
        dataStore.setTheme(theme)
    }

    private fun setDynamicColorsState(isEnabled: Boolean) = viewModelScope.launch {
        dataStore.setDynamicColorsState(isEnabled)
    }

    private fun sendEffect(effect: SettingsEffect) = viewModelScope.launch {
        _effect.emit(effect)
    }
}
