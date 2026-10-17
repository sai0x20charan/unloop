package com.charan.unloop.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.overlay.RoutineScheduler
import com.charan.unloop.core.utils.DateUtils
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.repository.InstalledAppsRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.models.InstalledAppsUiModel
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


    private fun initInstantSession() = viewModelScope.launch{
        val installedApps = installedAppsRepository.getInstalledApps()
        _state.update {
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    hour = 0,
                    minute = 0,
                    isHourValid = true,
                    isMinutesValid = true,
                    installedApps = installedApps.map { installedApp ->
                        InstalledAppsUiModel(
                            appName = installedApp.appName,
                            packageName = installedApp.packageName,
                            icon = installedApp.icon,
                            isSelected = false
                        )
                    }
                )
            )
        }
    }

    private fun observeActiveSession() = viewModelScope.launch {
        sessionRepository.getActiveSession().collectLatest { sessionEntity ->
            if (sessionEntity == null) {
                _state.update {
                    it.copy(
                        startSessionState = it.startSessionState.copy(
                            isRunning = false,
                            isPaused = false,
                            remainingSeconds = 0,
                            totalSeconds = 0
                        )
                    )
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

            _state.update {
                it.copy(
                    startSessionState = it.startSessionState.copy(
                        isRunning = isRunning,
                        isPaused = isPaused,
                        totalSeconds = totalSeconds,
                        remainingSeconds = DateUtils.getRemainingDuration(sessionEntity.endTime).toInt().coerceAtLeast(0)
                    )
                )
            }

            while (isRunning && isActive) {
                val remaining = DateUtils.getRemainingDuration(sessionEntity.endTime).toInt().coerceAtLeast(0)
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

    fun onEvent(event : HomeEvent) {
        when(event){
            is HomeEvent.OnAppClicked -> {
                handleAppClick(event.packageName)
            }
            is HomeEvent.OnHourChange -> {
                handleHourChange(event.hour)

            }
            is HomeEvent.OnPresetDurationSelect -> {
                handlePresetDurationSelect(event.preset)

            }
            is HomeEvent.OnMinuteChange -> {
                handleMinuteChange(event.minute)

            }

            HomeEvent.OnInstantSessionCustomizeToggle -> {
                handleSessionCustomizeToggle()

            }

            HomeEvent.OnSelectAllApps -> {
                handleSelectAllApps()
            }

            HomeEvent.OnEditSessionTime -> {
                handleEditSessionTime()

            }

            HomeEvent.OnStartSessionClick -> {
                handleStartSessionClick()

            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleStartSessionClick() = viewModelScope.launch{
        val hour = state.value.instantSessionUiModel.hour
        val minute = state.value.instantSessionUiModel.minute
        val statTime = DateUtils.getCurrentTime()
        val endTime = DateUtils.getEndTimeForDuration(state.value.instantSessionUiModel.totalSeconds)
        val blockedApps = state.value.instantSessionUiModel.installedApps.filter { it.isSelected }.map { it.packageName }
        val id = Uuid.generateV4().toString()
        sessionRepository.insertSession(
            SessionEntity(
                id = id,
                startTime = statTime,
                endTime = endTime,
                blockedApps = blockedApps,
                sessionStatus = SessionStatus.RUNNING,
                sessionType = SessionType.INSTANT,
                sessionName = "Instant Session",
                unlockChallengeType = UnlockChallengeType.STRICT_MODE
            )

        )
        routineScheduler.startSession(id)

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
            val allSelected = it.instantSessionUiModel.installedApps.all { app -> app.isSelected }
            val updatedApps = it.instantSessionUiModel.installedApps.map { app ->
                app.copy(isSelected = !allSelected)
            }
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    installedApps = updatedApps
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
            val updatedApps = it.instantSessionUiModel.installedApps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(isSelected = !app.isSelected)
                } else {
                    app
                }
            }
            it.copy(
                instantSessionUiModel = it.instantSessionUiModel.copy(
                    installedApps = updatedApps
                )
            )
        }
    }

    private fun handleHourChange(hour: String) {
        val digits = hour.filter(Char::isDigit)
        val parsedHour = if (digits.isEmpty()) 0 else digits.toIntOrNull() ?: 0
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
        val digits = minute.filter(Char::isDigit)
        val parsedMinute = if (digits.isEmpty()) 0 else digits.toIntOrNull() ?: 0
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

}
