package com.charan.unloop.presentation.routines

import com.charan.unloop.presentation.common.model.RoutineUiModel

sealed interface RoutinesEvent {
    data class OnToggleRoutine(val routineId: String, val isEnabled: Boolean) : RoutinesEvent
    data class OnDeleteClick(val routine: RoutineUiModel) : RoutinesEvent
    data object OnConfirmDelete : RoutinesEvent
    data object OnDismissDeleteDialog : RoutinesEvent
    data class OnEditRoutineClick(val routineId: String?) : RoutinesEvent
    data object OnBackClick : RoutinesEvent

}
