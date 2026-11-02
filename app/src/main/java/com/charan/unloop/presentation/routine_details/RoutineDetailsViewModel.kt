package com.charan.unloop.presentation.routine_details

import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.R
import com.charan.unloop.data.apps.InstalledAppsProvider
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.presentation.common.ToastMessage
import com.charan.unloop.presentation.common.mapper.toAppInfoUiModel
import com.charan.unloop.presentation.common.model.toggleAppSelection
import com.charan.unloop.presentation.common.model.toggleSelectAll
import com.charan.unloop.presentation.common.mapper.toRoutineEntity
import com.charan.unloop.presentation.common.mapper.toRoutineUiModel
import com.charan.unloop.session.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@HiltViewModel(assistedFactory = RoutineDetailsViewModel.Factory::class)
class RoutineDetailsViewModel @AssistedInject constructor(
    @Assisted("routineId") private val routineId: String?,
    private val installedAppsProvider: InstalledAppsProvider,
    private val routineRepository: RoutineRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("routineId") routineId: String?): RoutineDetailsViewModel
    }

    private val _state = MutableStateFlow(RoutineDetailsState(isEditing = routineId != null))
    val state = _state.asStateFlow()

    private val _effect = Channel<RoutineDetailsEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        initRoutineAndApps()
    }

    private fun initRoutineAndApps() = viewModelScope.launch {
        val routineUiModel = routineId?.let { routineRepository.getRoutineById(it)?.toRoutineUiModel() }
        val installedApps = installedAppsProvider.getLaunchableApps()
        val blockedSet = routineUiModel?.blockedAppPackages.orEmpty().toSet()

        _state.update { current ->
            current.copy(
                routine = routineUiModel ?: current.routine,
                installedApps = installedApps.map { app ->
                    app.toAppInfoUiModel(isSelected = app.packageName in blockedSet)
                }
            )
        }
    }

    fun onEvent(event: RoutineDetailsEvent) = viewModelScope.launch {
        when (event) {
            RoutineDetailsEvent.OnSaveRoutineClick -> handleSaveRoutineClick()
            is RoutineDetailsEvent.OnEndTimeChange -> handleEndTimeChange(event.hour, event.minute)
            RoutineDetailsEvent.OnEndTimeToggle -> handleShowEndTimeDialog()
            is RoutineDetailsEvent.OnRoutineDaySelect -> handleRoutineDaySelect(event.day)
            is RoutineDetailsEvent.OnRoutineNameChange -> handleRoutineNameChange(event.name)
            is RoutineDetailsEvent.OnStartTimeChange -> handleStartTimeChange(event.hour, event.minute)
            RoutineDetailsEvent.OnStartTimeToggle -> handleShowStartTimeDialog()
            is RoutineDetailsEvent.OnInstalledAppSelect -> handleInstalledAppSelect(event.packageName)
            RoutineDetailsEvent.OnSelectAllAppsClick -> handleSelectAllAppsClick()
            is RoutineDetailsEvent.OnUnlockChallengeTypeChange -> handleUnlockChallengeTypeChange(event.type)
            is RoutineDetailsEvent.OnWaitTimeSecondsChange -> handleWaitTimeSecondsChange(event.seconds)
            is RoutineDetailsEvent.OnSessionControlsLockedChange -> handleSessionControlLockChange(event.locked)
            RoutineDetailsEvent.OnScheduleToggle -> handleScheduleToggle()
            RoutineDetailsEvent.OnAppsSheetToggle -> {
                _state.update { it.copy(showAppsSheet = !it.showAppsSheet) }
            }
        }
    }

    private fun handleScheduleToggle() {
        _state.update {
            it.copy(
                isScheduleExpanded = !it.isScheduleExpanded
            )
        }
    }

    private fun handleSessionControlLockChange(locked: Boolean) {
        _state.update {
            it.copy(routine = it.routine.copy(areSessionControlsLocked = locked))
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

    private suspend fun handleSaveRoutineClick() {
        if (_state.value.isSaving || !validateRoutine()) return

        val state = _state.value
        val routine = state.routine.copy(
            name = state.routine.name.trim(),
            blockedAppPackages = state.installedApps.filter { it.isSelected }.map { it.packageName },
        )
        val routineEntity = routine.toRoutineEntity()
        _state.update { it.copy(isSaving = true) }

        try {
            routineRepository.upsertRoutine(routineEntity)
            _state.update { it.copy(routine = routine.copy(id = routineEntity.id)) }
            sessionManager.scheduleNextRoutineStart(routineEntity.id)
            _effect.send(RoutineDetailsEffect.OnBack)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            _effect.send(RoutineDetailsEffect.ShowToast(ToastMessage.Res(R.string.routine_save_failed)))
        } finally {
            _state.update { it.copy(isSaving = false) }
        }
    }

    private suspend fun validateRoutine(): Boolean {
        val state = _state.value
        val routine = state.routine

        val errorMessage = when {
            routine.name.isBlank() -> R.string.routine_name_required
            routine.selectedDays.isEmpty() -> R.string.routine_days_required
            state.installedApps.none { it.isSelected } -> R.string.routine_apps_required
            else -> null
        }

        if (errorMessage != null) {
            _state.update {
                it.copy(
                    isScheduleExpanded = it.isScheduleExpanded || routine.selectedDays.isEmpty(),
                    showAppsSheet = errorMessage == R.string.routine_apps_required,
                )
            }
            _effect.send(RoutineDetailsEffect.ShowToast(ToastMessage.Res(errorMessage)))
            return false
        }
        return true
    }

    private fun handleUnlockChallengeTypeChange(type: UnlockChallengeType) {
        _state.update { it.copy(routine = it.routine.copy(unlockChallengeType = type)) }
    }

    private fun handleWaitTimeSecondsChange(seconds: Int) {
        _state.update { it.copy(routine = it.routine.copy(waitDurationSeconds = seconds.coerceIn(5, 120))) }
    }
}
