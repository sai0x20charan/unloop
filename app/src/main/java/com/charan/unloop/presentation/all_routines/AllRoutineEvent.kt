package com.charan.unloop.presentation.all_routines

import com.charan.unloop.presentation.common.models.RoutineUiModel

sealed interface AllRoutineEvent {
    data class OnToggleRoutine(val routineId: String, val isEnabled: Boolean) : AllRoutineEvent
    data class OnDeleteClick(val routine: RoutineUiModel) : AllRoutineEvent
    data object OnConfirmDelete : AllRoutineEvent
    data object OnDismissDeleteDialog : AllRoutineEvent
    data object OnAddRoutineClick : AllRoutineEvent
    data object OnBackClick : AllRoutineEvent
}
