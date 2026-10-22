package com.charan.unloop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.overlay.RoutineSessionController
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.InstalledAppsRepository
import com.charan.unloop.data.repository.SessionManagerRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.models.toInstalledAppsUiModel
import com.charan.unloop.presentation.common.models.toggleAppSelection
import com.charan.unloop.presentation.common.models.toggleSelectAll
import com.charan.unloop.presentation.common.toCurrentSessionUiModel
import com.charan.unloop.presentation.routine.UnlockChallengeType
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val sessionRepository: SessionRepository,
    private val sessionManagerRepository: SessionManagerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadInstalledApps()
        observeActiveSession()
        loadLastInstantSession()
    }

    private fun loadLastInstantSession() = viewModelScope.launch {
        val lastInstantSession = sessionRepository.getLastInstantSession()
        if (lastInstantSession != null) {
            _state.update {
                it.copy(
                    instantSessionUiModel = it.instantSessionUiModel.copy(
                        sessionTitle = lastInstantSession.sessionName,
                        installedApps = it.instantSessionUiModel.installedApps.map { app ->
                            app.copy(
                                isSelected = lastInstantSession.blockedApps?.contains(app.packageName)
                                    ?: false
                            )
                        }
                    )
                )
            }
        }
    }

    private fun loadInstalledApps() = viewModelScope.launch {
        val installedApps = installedAppsRepository.getInstalledApps()
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    installedApps = installedApps.map { app -> app.toInstalledAppsUiModel() }
                )
            )
        }
    }

    private fun observeActiveSession() = viewModelScope.launch {
        sessionRepository.getActiveSession().collectLatest { sessionEntity ->
            if (sessionEntity == null) {
                _state.update {
                    it.copy(currentSessionUiModel = CurrentSessionUiModel())
                }
                return@collectLatest
            }

            if (DateTimeUtils.getRemainingDuration(sessionEntity.endTime) <= 0L) {
                sessionRepository.updateSessionStatus(
                    sessionId = sessionEntity.id,
                    status = SessionStatus.COMPLETED,
                    actualEndTime = DateTimeUtils.getCurrentDateTime()
                )
                sessionManagerRepository.stopSession(sessionEntity.id)
                return@collectLatest
            }

            val isRunning = sessionEntity.sessionStatus == SessionStatus.RUNNING
            _state.update {
                it.copy(
                    currentSessionUiModel = sessionEntity.toCurrentSessionUiModel()
                )
            }
            while (isRunning && isActive) {
                val remaining = DateTimeUtils.getRemainingDuration(
                    endTime = sessionEntity.endTime
                ).toInt().coerceAtLeast(0)
                _state.update {
                    it.copy(
                        currentSessionUiModel = it.currentSessionUiModel.copy(
                            remainingSeconds = remaining
                        )
                    )
                }
                if (remaining <= 0) {
                    sessionRepository.updateSessionStatus(
                        sessionId = sessionEntity.id,
                        status = SessionStatus.COMPLETED,
                        actualEndTime = DateTimeUtils.getCurrentDateTime()
                    )
                    sessionManagerRepository.stopSession(sessionEntity.id)
                    break
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
            is HomeEvent.OnPresetDurationSelect -> handlePresetDurationSelect(event.preset)
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
        }
    }

    private fun handlePauseSessionClick() = viewModelScope.launch {
        if (_state.value.currentSessionUiModel.isRunning) {
            sessionManagerRepository.pauseSession(
                sessionId = state.value.currentSessionUiModel.sessionId,
            )
        } else if (_state.value.currentSessionUiModel.isPaused) {
            sessionManagerRepository.resumeSession(state.value.currentSessionUiModel.sessionId)
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

    @OptIn(ExperimentalUuidApi::class)
    private fun handleStartSessionClick() = viewModelScope.launch {
        val uiModel = _state.value.instantSessionUiModel
        if (!uiModel.canStart) return@launch
        val totalSeconds = uiModel.totalSeconds.toLong()

        val startTime = DateTimeUtils.getCurrentDateTime()
        val endTime = DateTimeUtils.getEndTimeForDuration(totalSeconds)
        val blockedApps = uiModel.installedApps
            .filter { it.isSelected }
            .map { it.packageName }

        val id = Uuid.generateV4().toString()
        sessionRepository.insertSession(
            SessionEntity(
                id = id,
                actualStartTime = startTime,
                startTime = startTime,
                endTime = endTime,
                blockedApps = blockedApps,
                sessionStatus = SessionStatus.RUNNING,
                sessionType = SessionType.INSTANT,
                sessionName = uiModel.sessionTitle.trim().ifBlank { DEFAULT_SESSION_TITLE },
                unlockChallengeType = UnlockChallengeType.STRICT_MODE,
                createdAt = startTime
            )
        )
        sessionManagerRepository.startSession(id)
    }

    private fun handleEndSessionClick() = viewModelScope.launch {
        val activeSession = sessionRepository.getActiveSessionOnce() ?: return@launch
        sessionRepository.updateSessionStatus(
            sessionId = activeSession.id,
            status = SessionStatus.STOPPED,
            actualEndTime = DateTimeUtils.getCurrentDateTime()
        )
        sessionManagerRepository.stopSession(activeSession.id)
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

    private fun handlePresetDurationSelect(preset: Int) {
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    hour = preset / 60,
                    minute = preset % 60,
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
        const val DEFAULT_SESSION_TITLE = "Focus session"
        const val MAX_TITLE_LENGTH = 40
    }
}
