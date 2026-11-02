package com.charan.unloop.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BottomBarNavDestinations : NavKey {

    @Serializable
    data object Home : BottomBarNavDestinations()

    @Serializable
    @SerialName("com.charan.unloop.presentation.navigation.BottomBarNavDestinations.AllRoutines")
    data object Routines : BottomBarNavDestinations()

    @Serializable
    data object Settings : BottomBarNavDestinations()

    @Serializable
    data object Stats : BottomBarNavDestinations()
}

@Serializable
sealed class Destination : NavKey {

    @Serializable
    @SerialName("com.charan.unloop.presentation.navigation.Destination.OnBoarding")
    data object Onboarding : Destination()

    @Serializable
    @SerialName("com.charan.unloop.presentation.navigation.Destination.Routine")
    data class RoutineDetails(val routineId : String? = null) : Destination()

    @Serializable
    data object BottomBarNav : Destination()

    @Serializable
    @SerialName("com.charan.unloop.presentation.navigation.Destination.AboutLibraries")
    data object OpenSourceLicenses : Destination()

    @Serializable
    @SerialName("com.charan.unloop.presentation.navigation.Destination.Sessions")
    data class SessionHistory(val startDate: LocalDate, val endDate: LocalDate) : Destination()

    @Serializable
    @SerialName("com.charan.unloop.presentation.navigation.Destination.BlockedApps")
    data class AppBlockHistory(val startDate: LocalDate, val endDate: LocalDate) : Destination()

    @Serializable
    data class SessionDetails(val id : String) : Destination()
}
