package com.charan.unloop.presentation.routine

sealed interface RoutineEffect {
    object OnBack : RoutineEffect
    data class ShowToast(val message: String) : RoutineEffect
}
