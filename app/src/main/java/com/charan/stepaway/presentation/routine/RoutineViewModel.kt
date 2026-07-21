package com.charan.stepaway.presentation.routine

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.stepaway.data.repository.InstalledAppsRepository
import com.charan.stepaway.data.repository.RoutineRepository
import com.charan.stepaway.presentation.common.toRoutineEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val routineRepository: RoutineRepository

) : ViewModel() {

    private val _state = MutableStateFlow(RoutineState())
    val state = _state.asStateFlow()

    private val _effect = Channel<RoutineEffect>()
    val effect = _effect.receiveAsFlow()
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
            RoutineEvent.OnEndTimeToggle -> handleShowEndTimeDialog()
            RoutineEvent.OnNextClick -> handleNextClick()
            is RoutineEvent.OnRepeatIntervalChange -> handleRepeatIntervalChange(event.interval)
            is RoutineEvent.OnRoutineDaySelect -> handleRoutineDaySelect(event.day)
            is RoutineEvent.OnRoutineNameChange -> handleRoutineNameChange(event.name)
            is RoutineEvent.OnStartTimeChange -> handleStartTimeChange(event.hour, event.minute)
            RoutineEvent.OnStartTimeToggle -> handleShowStartTimeDialog()
            is RoutineEvent.OnInstalledAppSelect -> handleInstalledAppSelect(event.packageName)
            RoutineEvent.OnIntervalBlockingToggle -> handleIntervalBlockingToggle()

            is RoutineEvent.OnUnlockChallengeTypeChange -> handleUnlockChallengeTypeChange(event.type)
            is RoutineEvent.OnWaitTimeSecondsChange -> handleWaitTimeSecondsChange(event.seconds)
            is RoutineEvent.OnIntervalTimeChange -> handleIntervalChange(event.hours, event.minutes)
            RoutineEvent.OnIntervalTimeToggle -> handleShowIntervalDialog()
        }

    }


    private fun handleIntervalChange(hours: Int, minutes: Int) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(
                    intervalHours = hours,
                    intervalMinutes = minutes
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
            it.copy(showStartTimeDialog = !it.showStartTimeDialog)
        }
    }

    private fun handleShowEndTimeDialog() {
        _state.update {
            it.copy(showEndTimeDialog = !it.showEndTimeDialog)
        }
    }

    private fun handleShowIntervalDialog() {
        _state.update {
            it.copy(showIntervalDialog = !it.showIntervalDialog)
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

    private fun handleCreateRoutineClick() = viewModelScope.launch{
        if(isDataValid()){
            routineRepository.upsertRoutine(_state.value.toRoutineEntity())

        }
    }

    private fun isDataValid(): Boolean {
        val currentState = _state.value
        val scheduleState = currentState.routineScheduleState

        // Validate routine name
        if (currentState.routineName.isBlank()) {
            viewModelScope.launch {
                _effect.send(RoutineEffect.ShowToast("Routine name cannot be empty"))
            }
            return false
        }

        // Validate routine days
        if (scheduleState.routineDays.isEmpty()) {
            viewModelScope.launch {
                _effect.send(RoutineEffect.ShowToast("Please select at least one day for the routine"))
            }
            return false
        }

        // Validate start and end times if not all day
        if (!scheduleState.isAllDay) {
            if (scheduleState.routineStartTime >= scheduleState.routineEndTime) {
                viewModelScope.launch {
                    _effect.send(RoutineEffect.ShowToast("Start time must be before end time"))
                }
                return false
            }
        }

        return true
    }

    private fun handleUnlockChallengeTypeChange(type: UnlockChallengeType) {
        _state.update {
            it.copy(
                routineScheduleState = it.routineScheduleState.copy(unlockChallengeType = type),
                )
        }
    }

    private fun handleWaitTimeSecondsChange(seconds: Int) {
        _state.update {
            it.copy(routineScheduleState = it.routineScheduleState.copy(waitTimeSeconds = seconds))
        }
    }

}