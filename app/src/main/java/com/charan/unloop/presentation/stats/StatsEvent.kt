package com.charan.unloop.presentation.stats

sealed class StatsEvent {
    data object OnSeeAllSessions : StatsEvent()
    data object OnIncrementPeriod : StatsEvent()
    data object OnDecrementPeriod : StatsEvent()
    data class OnPeriodChange(val value : StatsPeriod) : StatsEvent()
}