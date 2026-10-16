package com.charan.unloop.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Destination : NavKey {
    @Serializable
    data class Routine(val routineId : String? = null) : Destination()

    @Serializable
    data object AllRoutines : Destination()
}
