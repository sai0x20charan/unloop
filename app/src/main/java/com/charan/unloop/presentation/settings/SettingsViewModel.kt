package com.charan.unloop.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.R
import com.charan.unloop.presentation.common.ToastMessage
import com.charan.unloop.core.utils.GITHUB_URL
import com.charan.unloop.core.utils.PLAY_STORE_URL
import com.charan.unloop.core.utils.getAppVersionWithVersionCode
import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.data.repository.BackupRepository
import com.charan.unloop.data.repository.DataStoreRepository
import com.charan.unloop.presentation.settings.SettingsEffect.*
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
    private val dataStore: DataStoreRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            dataStore.is24Hour.collect { is24Hour ->
                _state.update { it.copy(is24Hour = is24Hour) }
            }
        }
        viewModelScope.launch {
            dataStore.timeFormat.collect { format ->
                _state.update { it.copy(timeFormat = format) }
            }
        }
        observeSettingsDataStore()
        init()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnTimeFormatChange -> viewModelScope.launch {
                dataStore.setTimeFormat(event.format)
            }
            is SettingsEvent.OnThemeChange -> {
                changeTheme(event.theme)
            }
            is SettingsEvent.OnDynamicColorsChange -> {
                setDynamicColorsEnabled(event.isEnabled)
            }
            SettingsEvent.OnBack -> {
                sendEffect(OnBack)
            }

            SettingsEvent.OnSendFeedbackClick -> sendEffect(LaunchSendFeedbackEmail)
            SettingsEvent.OnRateAppClick -> sendEffect(OpenUrl(PLAY_STORE_URL))
            SettingsEvent.OnOpenSourceCodeClick -> sendEffect(OpenUrl(GITHUB_URL))

            SettingsEvent.OnOpenSourceLicensesClick -> sendEffect(OnNavigateToOpenSourceLicenses)
            SettingsEvent.OnExportDataClick -> sendEffect(SelectBackupDestination(backupRepository.suggestedBackupFileName))
            SettingsEvent.OnImportDataClick -> sendEffect(SelectBackupFile)
            is SettingsEvent.OnBackupDestinationSelected -> handleExport(event.uri)
            is SettingsEvent.OnBackupFileSelected -> handleImport(event.uri)
        }
    }

    private fun handleExport(destinationUri: Uri) = viewModelScope.launch {
        if (_state.value.isExportingData || _state.value.isImportingData) return@launch
        _state.update { it.copy(isExportingData = true) }
        val result = try {
            backupRepository.exportBackup(destinationUri)
        } finally {
            _state.update { it.copy(isExportingData = false) }
        }
        val message = if (result.isSuccess) {
            R.string.backup_export_success
        } else {
            R.string.backup_export_failed
        }
        _effect.emit(ShowToast(ToastMessage.Res(message)))
    }

    private fun handleImport(sourceUri: Uri) = viewModelScope.launch {
        if (_state.value.isExportingData || _state.value.isImportingData) return@launch
        _state.update { it.copy(isImportingData = true) }
        val result = try {
            backupRepository.importBackup(sourceUri)
        } finally {
            _state.update { it.copy(isImportingData = false) }
        }
        val message = if (result.isSuccess) {
            R.string.backup_import_success
        } else {
            R.string.backup_import_failed
        }
        _effect.emit(ShowToast(ToastMessage.Res(message)))
    }

    private fun init() {
        _state.update {
            it.copy(
                appVersion = getAppVersionWithVersionCode()
            )
        }
    }


    private fun observeSettingsDataStore() = viewModelScope.launch {
        combine(
            dataStore.theme,
            dataStore.isDynamicColorsEnabled
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

    private fun setDynamicColorsEnabled(isEnabled: Boolean) = viewModelScope.launch {
        dataStore.setDynamicColorsEnabled(isEnabled)
    }

    private fun sendEffect(effect: SettingsEffect) = viewModelScope.launch {
        _effect.emit(effect)
    }
}
