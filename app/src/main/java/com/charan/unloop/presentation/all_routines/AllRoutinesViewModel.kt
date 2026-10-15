package com.charan.unloop.presentation.all_routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.overlay.RoutineScheduler
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.presentation.common.models.RoutineUiModel
import com.charan.unloop.presentation.common.toRoutineUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllRoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val routineScheduler: RoutineScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(AllRoutinesState(isLoading = true))
    val state = _state.asStateFlow()

    private val _effect = Channel<AllRoutineEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeRoutines()
    }

    private fun observeRoutines() = viewModelScope.launch {
        routineRepository.getAllRoutines().collect { routines ->
            _state.update {
                it.copy(
                    allRoutines = routines.map { routine -> routine.toRoutineUiModel() },
                    isLoading = false
                )
            }
        }
    }

    fun onEvent(event: AllRoutineEvent) = viewModelScope.launch {
        when (event) {
            is AllRoutineEvent.OnToggleRoutine -> handleToggleRoutine(event.routineId, event.isEnabled)
            is AllRoutineEvent.OnDeleteClick -> _state.update { it.copy(routineToDelete = event.routine) }
            AllRoutineEvent.OnConfirmDelete -> handleConfirmDelete()
            AllRoutineEvent.OnDismissDeleteDialog -> _state.update { it.copy(routineToDelete = null) }
            AllRoutineEvent.OnAddRoutineClick -> _effect.send(AllRoutineEffect.NavigateToCreateRoutine)
            AllRoutineEvent.OnBackClick -> _effect.send(AllRoutineEffect.NavigateBack)
        }
    }

    private fun handleToggleRoutine(routineId: String, isEnabled: Boolean) = viewModelScope.launch {
        routineRepository.updateRoutineEnabled(routineId, isEnabled)
        val routine = routineRepository.getRoutineById(routineId)
        if (routine != null) {
            if (isEnabled) {
                if (!routine.daysOfWeek.isNullOrEmpty() && routine.startTime != null) {
                    routineScheduler.scheduleRoutine(
                        routineId = routine.id,
                        startTime = routine.startTime,
                        frequency = routine.daysOfWeek,
                        isAllDay = routine.isAllDay
                    )
                }
                _effect.send(AllRoutineEffect.ShowToast("${routine.name} enabled"))
            } else {
                routineScheduler.cancelRoutine(routineId)
                _effect.send(AllRoutineEffect.ShowToast("${routine.name} disabled"))
            }
        }
    }

    private fun handleConfirmDelete() = viewModelScope.launch {
        val routine = _state.value.routineToDelete ?: return@launch
        routineRepository.deleteRoutine(routine.id)
        routineScheduler.cancelRoutine(routine.id)
        _state.update { it.copy(routineToDelete = null) }
        _effect.send(AllRoutineEffect.ShowToast("Routine deleted"))
    }
}
