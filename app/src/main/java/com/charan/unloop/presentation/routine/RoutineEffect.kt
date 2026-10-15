package com.charan.unloop.presentation.routine

sealed interface RoutineEffect {
    object RoutineCreated : RoutineEffect
    data class ShowToast(val message: String) : RoutineEffect
}
