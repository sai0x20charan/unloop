package com.charan.unloop.presentation.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.overlay.RoutineScheduler
import com.charan.unloop.data.repository.InstalledAppsRepository
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.presentation.common.models.toInstalledAppsUiModel
import com.charan.unloop.presentation.common.models.toggleAppSelection
import com.charan.unloop.presentation.common.models.toggleSelectAll
import com.charan.unloop.presentation.common.toRoutineEntity
import com.charan.unloop.presentation.common.toRoutineUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@HiltViewModel(assistedFactory = RoutineViewModel.Factory::class)
class RoutineViewModel @AssistedInject constructor(
    @Assisted("routineId") private val routineId: String?,
    private val installedAppsRepository: InstalledAppsRepository,
    private val routineRepository: RoutineRepository,
    private val routineScheduler: RoutineScheduler
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("routineId") routineId: String?): RoutineViewModel
    }

    private val _state = MutableStateFlow(RoutineState())
    val state = _state.asStateFlow()

    private val _effect = Channel<RoutineEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        initRoutineAndApps()
    }

    private fun initRoutineAndApps() = viewModelScope.launch {
        val routineUiModel = routineId?.let { routineRepository.getRoutineById(it)?.toRoutineUiModel() }
        val installedApps = installedAppsRepository.getInstalledApps()
        val blockedSet = routineUiModel?.blockedApps.orEmpty().toSet()

        _state.update { current ->
            current.copy(
                routine = routineUiModel ?: current.routine,
                installedApps = installedApps.map { app ->
                    app.toInstalledAppsUiModel(isSelected = app.packageName in blockedSet)
                }
            )
        }
    }

    fun onEvent(event: RoutineEvent) = viewModelScope.launch {
        when (event) {
            RoutineEvent.OnBackClick -> handleBackClick()
            RoutineEvent.OnCreateRoutineClick -> handleCreateRoutineClick()
            is RoutineEvent.OnEndTimeChange -> handleEndTimeChange(event.hour, event.minute)
            RoutineEvent.OnEndTimeToggle -> handleShowEndTimeDialog()
            RoutineEvent.OnNextClick -> handleNextClick()
            is RoutineEvent.OnRoutineDaySelect -> handleRoutineDaySelect(event.day)
            is RoutineEvent.OnRoutineNameChange -> handleRoutineNameChange(event.name)
            is RoutineEvent.OnStartTimeChange -> handleStartTimeChange(event.hour, event.minute)
            RoutineEvent.OnStartTimeToggle -> handleShowStartTimeDialog()
            is RoutineEvent.OnInstalledAppSelect -> handleInstalledAppSelect(event.packageName)
            RoutineEvent.OnSelectAllAppsClick -> handleSelectAllAppsClick()
            is RoutineEvent.OnUnlockChallengeTypeChange -> handleUnlockChallengeTypeChange(event.type)
            is RoutineEvent.OnWaitTimeSecondsChange -> handleWaitTimeSecondsChange(event.seconds)
        }
    }

    private fun handleSelectAllAppsClick() {
        _state.update { state ->
            state.copy(installedApps = state.installedApps.toggleSelectAll())
        }
    }

    private fun handleInstalledAppSelect(packageName: String) {
        _state.update { state ->
            state.copy(installedApps = state.installedApps.toggleAppSelection(packageName))
        }
    }

    private fun handleShowStartTimeDialog() {
        _state.update { it.copy(showStartTimeDialog = !it.showStartTimeDialog) }
    }

    private fun handleShowEndTimeDialog() {
        _state.update { it.copy(showEndTimeDialog = !it.showEndTimeDialog) }
    }

    private fun handleRoutineNameChange(name: String) {
        _state.update { it.copy(routine = it.routine.copy(name = name)) }
    }

    private fun handleRoutineDaySelect(day: DayOfWeek) {
        _state.update {
            val currentDays = it.routine.selectedDays
            val updatedDays = if (day in currentDays) currentDays - day else currentDays + day
            it.copy(routine = it.routine.copy(selectedDays = updatedDays))
        }
    }

    private fun handleStartTimeChange(hour: Int, minute: Int) {
        _state.update {
            it.copy(
                routine = it.routine.copy(startTime = LocalTime(hour, minute)),
                showStartTimeDialog = false
            )
        }
    }

    private fun handleEndTimeChange(hour: Int, minute: Int) {
        _state.update {
            it.copy(
                routine = it.routine.copy(endTime = LocalTime(hour, minute)),
                showEndTimeDialog = false
            )
        }
    }

    private fun handleNextClick() {
        _state.update {
            if (it.pageState < it.totalPageState - 1) it.copy(pageState = it.pageState + 1) else it
        }
    }

    private fun handleBackClick() {
        _state.update {
            if (it.pageState > 0) it.copy(pageState = it.pageState - 1) else it
        }
    }

    private fun handleCreateRoutineClick() = viewModelScope.launch {
        if (!isDataValid()) return@launch

        _state.update {
            it.copy(
                routine = it.routine.copy(
                    blockedApps = it.installedApps.filter { app -> app.isSelected }.map { app -> app.packageName }
                )
            )
        }
        val routineEntity = _state.value.routine.toRoutineEntity()
        routineRepository.upsertRoutine(routineEntity)

        val daysOfWeek = routineEntity.daysOfWeek
        if (!daysOfWeek.isNullOrEmpty()) {
            routineScheduler.scheduleRoutine(
                routineId = routineEntity.id,
                startTime = routineEntity.startTime,
                frequency = daysOfWeek,
                isAllDay = routineEntity.isAllDay
            )
        }

        _effect.send(RoutineEffect.OnBack)
    }

    private suspend fun isDataValid(): Boolean {
        val routine = _state.value.routine
        val startTime = routine.startTime
        val endTime = routine.endTime

        val errorMessage = when {
            routine.name.isBlank() -> "Routine name cannot be empty"
            routine.selectedDays.isEmpty() -> "Please select at least one day for the routine"
            startTime == null || endTime == null -> "Please select start and end times"
            startTime >= endTime -> "Start time must be before end time"
            else -> null
        }

        if (errorMessage != null) {
            _effect.send(RoutineEffect.ShowToast(errorMessage))
            return false
        }
        return true
    }

    private fun handleUnlockChallengeTypeChange(type: UnlockChallengeType) {
        _state.update { it.copy(routine = it.routine.copy(unlockChallengeType = type)) }
    }

    private fun handleWaitTimeSecondsChange(seconds: Int) {
        _state.update { it.copy(routine = it.routine.copy(waitDurationSeconds = seconds)) }
    }
}
