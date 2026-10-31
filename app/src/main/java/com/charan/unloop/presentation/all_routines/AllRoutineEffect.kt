package com.charan.unloop.presentation.all_routines

import com.charan.unloop.presentation.common.ToastMessage

sealed interface AllRoutineEffect {
    data class ShowToast(val message: ToastMessage) : AllRoutineEffect
    data class NavigateToEditRoutine(val routineId: String?) : AllRoutineEffect
    data object NavigateBack : AllRoutineEffect

}
