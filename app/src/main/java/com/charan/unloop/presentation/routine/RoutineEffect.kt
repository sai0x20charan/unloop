package com.charan.unloop.presentation.routine

import androidx.annotation.StringRes

sealed interface RoutineEffect {
    object OnBack : RoutineEffect
    data class ShowToast(@param:StringRes val messageRes: Int) : RoutineEffect
}
