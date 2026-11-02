package com.charan.unloop.presentation.routines

import com.charan.unloop.presentation.common.ToastMessage

sealed interface RoutinesEffect {
    data class ShowToast(val message: ToastMessage) : RoutinesEffect
    data class NavigateToRoutineDetails(val routineId: String?) : RoutinesEffect
    data object NavigateBack : RoutinesEffect

}
