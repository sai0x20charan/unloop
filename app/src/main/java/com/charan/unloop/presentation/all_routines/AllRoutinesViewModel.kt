package com.charan.unloop.presentation.all_routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.presentation.common.toRoutineUiModel
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
class AllRoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AllRoutinesState())
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
                )
            }
        }
    }

    fun onEvent(event: AllRoutineEvent) = viewModelScope.launch {
        when (event) {
            is AllRoutineEvent.OnToggleRoutine -> handleToggleRoutine(event.routineId, event.isEnabled)
            is AllRoutineEvent.OnDeleteClick -> _state.update { it.copy() }
            AllRoutineEvent.OnConfirmDelete -> handleConfirmDelete()
            AllRoutineEvent.OnDismissDeleteDialog -> _state.update { it.copy() }
            is AllRoutineEvent.OnEditRoutineClick -> handleRoutineSelect(event.routineId)
            AllRoutineEvent.OnBackClick -> _effect.send(AllRoutineEffect.NavigateBack)
        }
    }

    private fun handleRoutineSelect(routineId : String?) {
        sendEffect(AllRoutineEffect.NavigateToEditRoutine(routineId))
    }

    private fun handleToggleRoutine(routineId: String, isEnabled: Boolean) = viewModelScope.launch {
        routineRepository.updateRoutineEnabled(routineId, isEnabled)
        val routine = routineRepository.getRoutineById(routineId)
        if (routine != null) {
            if (isEnabled) {
                sessionManager.scheduleSession(routine.id)
                _effect.send(AllRoutineEffect.ShowToast("${routine.name} enabled"))
            } else {
                sessionManager.cancelScheduledRoutine(routine.id)
                _effect.send(AllRoutineEffect.ShowToast("${routine.name} disabled"))
            }
        }
    }

    private fun handleConfirmDelete() = viewModelScope.launch {
    }

    private fun sendEffect(effect: AllRoutineEffect) = viewModelScope.launch {
        _effect.send(effect)
    }
}
