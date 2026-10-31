package com.charan.unloop.presentation.routine

import com.charan.unloop.presentation.common.ToastMessage

sealed interface RoutineEffect {
    object OnBack : RoutineEffect
    data class ShowToast(val message: ToastMessage) : RoutineEffect
}
