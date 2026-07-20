package com.charan.stepaway.presentation.routine

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.stepaway.data.repository.InstalledAppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository

) : ViewModel() {

    private val _state = MutableStateFlow(RoutineState())
    val state = _state.asStateFlow()
    init {
        initInstalledApps()
    }


    private fun initInstalledApps() = viewModelScope.launch {

        val installedApps = installedAppsRepository.getInstalledApps()
        _state.update {
            it.copy(installedApps = installedApps.map { app ->
                InstalledAppsUiModel(
                    appName = app.appName,
                    packageName = app.packageName,
                    icon = app.icon,
                    isSelected = false
                )
            })
        }
    }


    fun onEvent(event : RoutineEvent) = viewModelScope.launch {

        when(event){
            RoutineEvent.OnAllDayToggle -> handleAllDayToggle()
            RoutineEvent.OnBackClick -> handleBackClick()
            RoutineEvent.OnCreateRoutineClick -> handleCreateRoutineClick()
            is RoutineEvent.OnEndTimeChange -> handleEndTimeChange(event.hour, event.minute)
            RoutineEvent.OnEndTimeClick -> handleShowEndTimeDialog()
            RoutineEvent.OnNextClick -> handleNextClick()
            is RoutineEvent.OnRepeatIntervalChange -> handleRepeatIntervalChange(event.interval)
            is RoutineEvent.OnRoutineDaySelect -> handleRoutineDaySelect(event.day)
            is RoutineEvent.OnRoutineNameChange -> handleRoutineNameChange(event.name)
            is RoutineEvent.OnStartTimeChange -> handleStartTimeChange(event.hour, event.minute)
            RoutineEvent.OnStartTimeClick -> handleShowStartTimeDialog()
            RoutineEvent.OnStartTimeDialogDismiss -> handleStartTimeDialogDismiss()
            RoutineEvent.OnEndTimeDialogDismiss -> handleEndTimeDialogDismiss()
            is RoutineEvent.OnInstalledAppSelect -> handleInstalledAppSelect(event.packageName)
            RoutineEvent.OnIntervalBlockingToggle -> handleIntervalBlockingToggle()
            is RoutineEvent.OnIntervalHoursChange -> handleIntervalHoursChange(event.hours)
            is RoutineEvent.OnIntervalMinutesChange -> handleIntervalMinutesChange(event.minutes)
            is RoutineEvent.OnLockDurationHoursChange -> handleLockDurationHoursChange(event.hours)
            is RoutineEvent.OnLockDurationMinutesChange -> handleLockDurationMinutesChange(event.minutes)
            is RoutineEvent.OnUnlockChallengeTypeChange -> handleUnlockChallengeTypeChange(event.type)
            is RoutineEvent.OnWaitTimeSecondsChange -> handleWaitTimeSecondsChange(event.seconds)
            is RoutineEvent.OnOneTimeUnlockWaitTimeChange -> handleOneTimeUnlockWaitTimeChange(event.hour, event.minute)
            RoutineEvent.OnOneTimeUnlockTimeClick -> handleShowOneTimeUnlockTimeDialog()
            RoutineEvent.OnOneTimeUnlockTimeDialogDismiss -> handleOneTimeUnlockTimeDialogDismiss()
        }

    }

    private fun handleIntervalHoursChange(hours: Int) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(
                    intervalHours = hours
                )
            )
        }
    }

    private fun handleIntervalMinutesChange(minutes: Int) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(
                    intervalMinutes = minutes
                )
            )
        }
    }

    private fun handleLockDurationHoursChange(hours: Int) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(
                    lockDurationHours = hours
                )
            )
        }
    }

    private fun handleLockDurationMinutesChange(minutes: Int) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(
                    lockDurationMinutes = minutes
                )
            )
        }
    }

    private fun handleIntervalBlockingToggle() {
        _state.update {
            val isIntervalBlocking = !it.routineScheduleState.isIntervalBlocking
            it.copy(routineScheduleState = it.routineScheduleState.copy(isIntervalBlocking = isIntervalBlocking))
        }
    }

    private fun handleInstalledAppSelect(packageName: String) {
        _state.update {
            val updatedApps = it.installedApps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(isSelected = !app.isSelected)
                } else {
                    app
                }
            }
            it.copy(installedApps = updatedApps)
        }
    }

    private fun handleShowStartTimeDialog() {
        _state.update {
            it.copy(showStartTimeDialog = true)
        }
    }

    private fun handleShowEndTimeDialog() {
        _state.update {
            it.copy(showEndTimeDialog = true)
        }
    }

    private fun handleRoutineNameChange(name: String) {
       _state.update {
            it.copy(routineName = name)
       }
    }

    private fun handleRoutineDaySelect(day: DayOfWeek) {
        _state.update {
            val currentDays = it.routineScheduleState.routineDays
            val updatedDays = if (currentDays.contains(day)) {
                currentDays - day
            } else {
                currentDays + day
            }
            it.copy(routineScheduleState = it.routineScheduleState.copy(routineDays = updatedDays))
        }
    }

    private fun handleAllDayToggle() {
        _state.update {
            val isAllDay = !it.routineScheduleState.isAllDay
            it.copy(routineScheduleState = it.routineScheduleState.copy(isAllDay = isAllDay))
        }
    }

    private fun handleStartTimeChange(hour: Int, minute: Int) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(
                    routineStartTime = LocalTime(hour, minute)
                ),
                showStartTimeDialog = false
            )
        }
    }

    private fun handleEndTimeChange(hour: Int, minute: Int) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(
                    routineEndTime = LocalTime(hour, minute)
                ),
                showEndTimeDialog = false
            )
        }
    }

    private fun handleStartTimeDialogDismiss() {
        _state.update {
            it.copy(showStartTimeDialog = false)
        }
    }

    private fun handleEndTimeDialogDismiss() {
        _state.update {
            it.copy(showEndTimeDialog = false)
        }
    }

    private fun handleRepeatIntervalChange(interval: Int) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(
                    routineFrequency = interval
                )
            )
        }
    }

    private fun handleNextClick() {
        _state.update {
            if (it.pageState < it.totalPageState - 1) {
                it.copy(pageState = it.pageState + 1)
            } else {
                it
            }
        }
    }

    private fun handleBackClick() {
        _state.update {
            if (it.pageState > 0) {
                it.copy(pageState = it.pageState - 1)
            } else {
                it
            }
        }
    }

    private fun handleCreateRoutineClick() {
        // Log creation or insert to database in future
    }

    private fun handleUnlockChallengeTypeChange(type: UnlockChallengeType) {
        _state.update {
            it.copy(
                unlockChallengeType = type,
                showOneTimeUnlockTimeDialog = type == UnlockChallengeType.ONE_TIME_UNLOCK
            )
        }
    }

    private fun handleWaitTimeSecondsChange(seconds: Int) {
        _state.update {
            it.copy(waitTimeSeconds = seconds)
        }
    }

    private fun handleOneTimeUnlockWaitTimeChange(hour: Int, minute: Int) {
        _state.update {
            it.copy(
                oneTimeUnlockWaitTimeHours = hour,
                oneTimeUnlockWaitTimeMinutes = minute,
                showOneTimeUnlockTimeDialog = false
            )
        }
    }

    private fun handleShowOneTimeUnlockTimeDialog() {
        _state.update {
            it.copy(showOneTimeUnlockTimeDialog = true)
        }
    }

    private fun handleOneTimeUnlockTimeDialogDismiss() {
        _state.update {
            it.copy(showOneTimeUnlockTimeDialog = false)
        }
    }
}