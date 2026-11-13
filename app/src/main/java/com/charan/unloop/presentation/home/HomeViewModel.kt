package com.charan.unloop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.apps.InstalledAppsProvider
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.repository.DataStoreRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.mapper.toAppInfoUiModel
import com.charan.unloop.presentation.common.model.toggleAppSelection
import com.charan.unloop.presentation.common.model.toggleSelectAll
import com.charan.unloop.presentation.common.mapper.toCurrentSessionUiModel
import com.charan.unloop.presentation.common.mapper.toInstantSessionUiModel
import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import com.charan.unloop.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val installedAppsProvider: InstalledAppsProvider,
    private val sessionRepository: SessionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreRepository.is24Hour.collect { is24Hour ->
                _state.update { it.copy(is24Hour = is24Hour) }
            }
        }
        loadInstantSessionAndApps()
        observeActiveSession()
    }

    private fun loadInstantSessionAndApps() = viewModelScope.launch {
        val lastInstantSession = sessionRepository.getLastInstantSession()
        val installedApps = installedAppsProvider.getLaunchableApps().map { app ->
            app.toAppInfoUiModel()
        }
        _state.update {
            it.copy(
                instantSessionUiModel = lastInstantSession?.toInstantSessionUiModel(installedApps)
                    ?: it.instantSessionUiModel.copy(installedApps = installedApps)
            )
        }
    }

    private fun observeActiveSession() = viewModelScope.launch {
        sessionRepository.activeSession.collectLatest { sessionEntity ->
            if (sessionEntity == null) {
                _state.update {
                    it.copy(currentSessionUiModel = CurrentSessionUiModel())
                }
                return@collectLatest
            }

            val isRunning = sessionEntity.status == SessionStatus.RUNNING
            _state.update {
                it.copy(
                    currentSessionUiModel = sessionEntity.toCurrentSessionUiModel(),
                    instantSessionUiModel = it.instantSessionUiModel.copy(isTitleEditable = false),
                )
            }
            while (isRunning && isActive) {
                val remaining = DateTimeUtils.getRemainingSeconds(
                    endTime = sessionEntity.scheduledEndTime
                ).toInt().coerceAtLeast(0)
                _state.update {
                    it.copy(
                        currentSessionUiModel = it.currentSessionUiModel.copy(
                            remainingSeconds = remaining
                        )
                    )
                }
                delay(1000.milliseconds)
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnAppClicked -> handleAppClick(event.packageName)
            is HomeEvent.OnHourChange -> handleHourChange(event.hour)
            is HomeEvent.OnMinuteChange -> handleMinuteChange(event.minute)
            is HomeEvent.OnPresetDurationSelect -> handlePresetDurationSelect(event.durationMinutes)
            HomeEvent.OnSelectAllApps -> handleSelectAllApps()
            HomeEvent.OnInstantSessionCustomizeToggle -> handleSessionCustomizeToggle()
            HomeEvent.OnAppsSheetDismiss -> _state.update {
                it.copy(instantSessionUiModel = it.instantSessionUiModel.copy(showBottomSheet = false))
            }
            HomeEvent.OnStartSessionClick -> handleStartSessionClick()
            HomeEvent.OnEditSessionTime -> handleEditSessionTime()
            is HomeEvent.OnSessionTitleChange -> handleSessionTitleChange(event.title)
            HomeEvent.OnPauseSessionClick -> handlePauseSessionClick()
            HomeEvent.OnEndSessionClick -> handleEndSessionClick()
            HomeEvent.OnEditTitleClick -> handleEditTitleClick()
            HomeEvent.OnSessionControlsLockToggle -> handleSessionControlsLockToggle()
            HomeEvent.OnWaitTimeToggle -> handleWaitTimeToggle()
            is HomeEvent.OnWaitTimeChange -> handleWaitTimeChange(event.seconds)
        }
    }

    private fun handleWaitTimeChange(seconds : Int) = viewModelScope.launch {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    waitDurationSeconds = seconds
                )
            )

        }

    }

    private fun handleWaitTimeToggle() = viewModelScope.launch {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    unlockChallengeType = if (it.instantSessionUiModel.unlockChallengeType == UnlockChallengeType.STRICT_MODE) {
                        UnlockChallengeType.WAIT_TIME
                    } else {
                        UnlockChallengeType.STRICT_MODE
                    }
                )
            )
        }
    }

    private fun handleSessionControlsLockToggle() = viewModelScope.launch {
        _state.update {
            it.copy(
                instantSessionUiModel =  it.instantSessionUiModel.copy(
                    areSessionControlsLocked = !it.instantSessionUiModel.areSessionControlsLocked
                )
            )
        }
    }

    private fun handleEditTitleClick() = viewModelScope.launch {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    isTitleEditable = !it.instantSessionUiModel.isTitleEditable
                )
            )
        }
    }

    private fun handlePauseSessionClick() = viewModelScope.launch {
        if (_state.value.currentSessionUiModel.isRunning) {
            sessionManager.pauseSession(
                sessionId = state.value.currentSessionUiModel.sessionId,
            )
        } else if (_state.value.currentSessionUiModel.isPaused) {
            sessionManager.resumeSession(state.value.currentSessionUiModel.sessionId)
        }
    }

    private fun handleSessionTitleChange(title: String) {
        if (title.length <= MAX_TITLE_LENGTH) {
            _state.update {
                it.copy(
                    instantSessionUiModel = it.instantSessionUiModel.copy(
                        sessionTitle = title
                    )
                )
            }
        }
    }

    private fun handleStartSessionClick() = viewModelScope.launch {
        val uiModel = _state.value.instantSessionUiModel
        if (!uiModel.canStart) return@launch
        val blockedAppPackages = uiModel.installedApps
            .filter { it.isSelected }
            .map { it.packageName }

        sessionManager.startInstantSession(
            name = uiModel.sessionTitle,
            durationSeconds = uiModel.totalSeconds.toLong(),
            blockedAppPackages = blockedAppPackages,
            areSessionControlsLocked = uiModel.areSessionControlsLocked,
            unlockChallengeType = uiModel.unlockChallengeType,
            waitDurationSeconds = uiModel.waitDurationSeconds,
        )
    }

    private fun handleEndSessionClick() = viewModelScope.launch {
        sessionManager.stopSession(_state.value.currentSessionUiModel.sessionId)
    }

    private fun handleEditSessionTime() {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    isSessionTimeEditing = !it.instantSessionUiModel.isSessionTimeEditing
                )
            )
        }
    }

    private fun handleSelectAllApps() {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    installedApps = it.instantSessionUiModel.installedApps.toggleSelectAll()
                )
            )
        }
    }

    private fun handlePresetDurationSelect(durationMinutes: Int) {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    hour = durationMinutes / 60,
                    minute = durationMinutes % 60,
                    isHourValid = true,
                    isMinutesValid = true,
                )
            )
        }
    }

    private fun handleSessionCustomizeToggle() {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    showBottomSheet = !it.instantSessionUiModel.showBottomSheet
                )
            )
        }
    }

    private fun handleAppClick(packageName: String) {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    installedApps = it.instantSessionUiModel.installedApps.toggleAppSelection(packageName)
                )
            )
        }
    }

    private fun handleHourChange(hour: String) {
        val parsedHour = hour.filter(Char::isDigit).toIntOrNull() ?: 0
        if (parsedHour !in 0..999) return

        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    hour = parsedHour,
                    isHourValid = true,
                    isMinutesValid = true,
                )
            )
        }
    }

    private fun handleMinuteChange(minute: String) {
        val parsedMinute = minute.filter(Char::isDigit).toIntOrNull() ?: 0
        if (parsedMinute !in 0..999) return

        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    minute = parsedMinute,
                    isHourValid = true,
                    isMinutesValid = true,
                )
            )
        }
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 40
    }
}
