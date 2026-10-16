package com.charan.unloop.presentation.all_routines

sealed interface AllRoutineEffect {
    data class ShowToast(val message: String) : AllRoutineEffect
    data class NavigateToEditRoutine(val routineId: String?) : AllRoutineEffect
    data object NavigateBack : AllRoutineEffect

}
