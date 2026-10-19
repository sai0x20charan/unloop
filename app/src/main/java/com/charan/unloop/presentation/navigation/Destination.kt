package com.charan.unloop.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class BottomBarNavDestinations : NavKey {


    @Serializable
    data object Home : BottomBarNavDestinations()

    @Serializable
    data object AllRoutines : BottomBarNavDestinations()

    @Serializable
    data object Profile : BottomBarNavDestinations()
}

@Serializable
sealed class Destination : NavKey {

    @Serializable
    data object OnBoarding : Destination()
    @Serializable
    data class Routine(val routineId : String? = null) : Destination()

    @Serializable
    data object BottomBarNav : Destination()
}
