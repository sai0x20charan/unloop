package com.charan.unloop.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        observeAppState()
    }

    private fun observeAppState() = viewModelScope.launch {
        val initialTheme = dataStoreRepository.theme.first()
        val initialDynamic = dataStoreRepository.isDynamicColorsEnabled.first()
        val initialOnboarding = dataStoreRepository.isOnboardingCompleted.first()

        _state.update {
            it.copy(
                isLoading = false,
                theme = initialTheme,
                dynamicColorsEnabled = initialDynamic,
                isOnboardingCompleted = initialOnboarding
            )
        }

        launch {
            combine(
                dataStoreRepository.theme,
                dataStoreRepository.isDynamicColorsEnabled
            ) { theme, dynamicColorsEnabled ->
                theme to dynamicColorsEnabled
            }.collectLatest { (theme, dynamicColorsEnabled) ->
                _state.update {
                    it.copy(
                        theme = theme,
                        dynamicColorsEnabled = dynamicColorsEnabled
                    )
                }
            }
        }
    }
}
