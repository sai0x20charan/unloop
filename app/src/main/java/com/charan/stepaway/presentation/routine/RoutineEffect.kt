package com.charan.stepaway.presentation.routine

sealed interface RoutineEffect {
    object RoutineCreated : RoutineEffect
    data class ShowToast(val message: String) : RoutineEffect
}
