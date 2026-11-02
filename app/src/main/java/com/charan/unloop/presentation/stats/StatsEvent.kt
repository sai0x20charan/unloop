package com.charan.unloop.presentation.stats

import androidx.compose.runtime.State

sealed class StatsEvent {
    data object OnSeeAllSessions : StatsEvent()
    data object OnIncrementPeriod : StatsEvent()
    data object OnDecrementPeriod : StatsEvent()
    data class OnPeriodChange(val value : StatsPeriod) : StatsEvent()

    data class OnSessionDetails(val id : String) : StatsEvent()
}