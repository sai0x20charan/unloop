package com.charan.unloop.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreRepository.is24Hour.collect { is24Hour ->
                _state.update { it.copy(is24Hour = is24Hour) }
            }
        }
        observeAppState()
    }

    private fun observeAppState() = viewModelScope.launch {
        combine(
            dataStoreRepository.theme,
            dataStoreRepository.isDynamicColorsEnabled,
            dataStoreRepository.isAmoledThemeEnabled,
            dataStoreRepository.isHapticsEnabled,
            dataStoreRepository.isOnboardingCompleted
        ) { theme, dynamic, amoled, haptics, onboarding ->
            _state.update {
                it.copy(
                    isLoading = false,
                    theme = theme,
                    dynamicColorsEnabled = dynamic,
                    isAmoledThemeEnabled = amoled,
                    isHapticsEnabled = haptics,
                    isOnboardingCompleted = onboarding
                )
            }
        }.collect()
    }
}
