package com.charan.unloop.presentation.routine_details

import com.charan.unloop.presentation.common.ToastMessage

sealed interface RoutineDetailsEffect {
    object OnBack : RoutineDetailsEffect
    data class ShowToast(val message: ToastMessage) : RoutineDetailsEffect
}
