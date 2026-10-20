package com.charan.unloop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.overlay.RoutineScheduler
import com.charan.unloop.core.utils.DateUtils
import com.charan.unloop.core.utils.DateUtils.toDisplayString
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.InstalledAppsRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.models.toInstalledAppsUiModel
import com.charan.unloop.presentation.common.models.toggleAppSelection
import com.charan.unloop.presentation.common.models.toggleSelectAll
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
    private val routineScheduler: RoutineScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        initInstantSession()
        observeActiveSession()
    }

    private fun initInstantSession() = viewModelScope.launch {
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
                    it.copy(startSessionState = StartSessionState())
                }
                return@collectLatest
            }

            val isRunning = sessionEntity.sessionStatus == SessionStatus.RUNNING
            val isPaused = sessionEntity.sessionStatus == SessionStatus.PAUSED

            val startSec = sessionEntity.startTime.toSecondOfDay()
            val endSec = sessionEntity.endTime.toSecondOfDay()
            val totalSeconds = if (endSec >= startSec) {
                endSec - startSec
            } else {
                (86400 - startSec) + endSec
            }

            val remainingSeconds = if (isPaused && sessionEntity.remainingSeconds != null) {
                sessionEntity.remainingSeconds
            } else {
                DateUtils.getRemainingDuration(
                    startTime = sessionEntity.startTime,
                    endTime = sessionEntity.endTime
                ).toInt().coerceAtLeast(0)
            }

            _state.update {
                it.copy(
                    startSessionState = it.startSessionState.copy(
                        isRunning = isRunning,
                        isPaused = isPaused,
                        sessionName = sessionEntity.sessionName,
                        endTimeFormatted = sessionEntity.endTime.toDisplayString(),
                        blockedPackages = sessionEntity.blockedApps.orEmpty(),
                        totalSeconds = totalSeconds,
                        remainingSeconds = remainingSeconds,
                        sessionId = sessionEntity.id
                    )
                )
            }

            while (isRunning && isActive) {
                val remaining = DateUtils.getRemainingDuration(
                    startTime = sessionEntity.startTime,
                    endTime = sessionEntity.endTime
                ).toInt().coerceAtLeast(0)
                _state.update {
                    it.copy(
                        startSessionState = it.startSessionState.copy(
                            remainingSeconds = remaining
                        )
                    )
                }
                if (remaining <= 0) break
                delay(1000.milliseconds)
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnAppClicked -> handleAppClick(event.packageName)
            is HomeEvent.OnHourChange -> handleHourChange(event.hour)
            is HomeEvent.OnPresetDurationSelect -> handlePresetDurationSelect(event.preset)
            is HomeEvent.OnMinuteChange -> handleMinuteChange(event.minute)
            HomeEvent.OnInstantSessionCustomizeToggle -> handleSessionCustomizeToggle()
            HomeEvent.OnSelectAllApps -> handleSelectAllApps()
            HomeEvent.OnEditSessionTime -> handleEditSessionTime()
            HomeEvent.OnStartSessionClick -> handleStartSessionClick()
            HomeEvent.OnEndSessionClick -> handleEndSessionClick()
            is HomeEvent.OnSessionTitleChange -> handleSessionTitleChange(event.title)
            HomeEvent.OnAppsSheetDismiss -> _state.update {
                it.copy(instantSessionUiModel = it.instantSessionUiModel.copy(showBottomSheet = false))
            }

            is HomeEvent.OnPauseSessionClick -> {
                handlePauseSessionClick()
            }
        }
    }

    private fun handlePauseSessionClick() = viewModelScope.launch {
        if (_state.value.startSessionState.isRunning) {
            routineScheduler.pauseRoutine(
                sessionId = state.value.startSessionState.sessionId,
                remainingSeconds = state.value.startSessionState.remainingSeconds
            )
        } else if (_state.value.startSessionState.isPaused) {
            val remainingSeconds = state.value.startSessionState.remainingSeconds
            val newEndTime = DateUtils.getEndTimeForDuration(remainingSeconds)
            _state.update {
                it.copy(
                    startSessionState = it.startSessionState.copy(
                        endTimeFormatted = newEndTime.toDisplayString()
                    )
                )
            }
            routineScheduler.resumeRoutine(state.value.startSessionState.sessionId)
        }
    }

    private fun handleSessionTitleChange(title: String) {
        if (title.length > MAX_TITLE_LENGTH) return
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(sessionTitle = title)
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleStartSessionClick() = viewModelScope.launch {
        val uiModel = state.value.instantSessionUiModel
        if (!uiModel.canStart) return@launch
        val totalSeconds = uiModel.totalSeconds

        val startTime = DateUtils.getCurrentTime()
        val endTime = DateUtils.getEndTimeForDuration(totalSeconds)
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
                unlockChallengeType = UnlockChallengeType.STRICT_MODE
            )
        )
        routineScheduler.startSession(id)
    }

    private fun handleEndSessionClick() = viewModelScope.launch {
        val activeSession = sessionRepository.getActiveSessionOnce() ?: return@launch
        sessionRepository.updateSessionStatus(
            sessionId = activeSession.id,
            status = SessionStatus.STOPPED,
            actualEndTime = DateUtils.getCurrentTime()
        )
        routineScheduler.stopSession(activeSession.id)
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
