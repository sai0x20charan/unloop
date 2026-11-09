package com.charan.unloop.presentation.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.data.repository.DataStoreRepository
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.presentation.common.mapper.toRoutineUiModel
import com.charan.unloop.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val routineRepository: RoutineRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(RoutinesState())
    val state = _state.asStateFlow()

    private val _effect = Channel<RoutinesEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            dataStoreRepository.is24Hour.collect { is24Hour ->
                _state.update { it.copy(is24Hour = is24Hour) }
            }
        }
        observeRoutines()
    }

    private fun observeRoutines() = viewModelScope.launch {
        routineRepository.getAllRoutines().collect { routines ->
            _state.update {
                it.copy(
                    allRoutines = routines.map { routine -> routine.toRoutineUiModel() },
                )
            }
        }
    }

    fun onEvent(event: RoutinesEvent) = viewModelScope.launch {
        when (event) {
            is RoutinesEvent.OnToggleRoutine -> handleToggleRoutine(event.routineId, event.isEnabled)
            is RoutinesEvent.OnDeleteClick -> _state.update { it.copy() }
            RoutinesEvent.OnConfirmDelete -> handleConfirmDelete()
            RoutinesEvent.OnDismissDeleteDialog -> _state.update { it.copy() }
            is RoutinesEvent.OnEditRoutineClick -> handleRoutineSelect(event.routineId)
            RoutinesEvent.OnBackClick -> _effect.send(RoutinesEffect.NavigateBack)
        }
    }

    private fun handleRoutineSelect(routineId : String?) {
        sendEffect(RoutinesEffect.NavigateToRoutineDetails(routineId))
    }

    private fun handleToggleRoutine(routineId: String, isEnabled: Boolean) = viewModelScope.launch {
        routineRepository.updateRoutineEnabled(routineId, isEnabled)
        val routine = routineRepository.getRoutineById(routineId)
        if (routine != null) {
            if (isEnabled) {
                sessionManager.scheduleNextRoutineStart(routine.id)
            } else {
                sessionManager.cancelScheduledRoutine(routine.id)
            }
        }
    }

    private fun handleConfirmDelete() = viewModelScope.launch {
    }

    private fun sendEffect(effect: RoutinesEffect) = viewModelScope.launch {
        _effect.send(effect)
    }
}
