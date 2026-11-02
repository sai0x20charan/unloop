package com.charan.unloop.presentation.blocked_apps

import kotlinx.datetime.LocalDate

sealed interface BlockedAppsEvent {
    data class OnDateRangeChange(val startDate: LocalDate, val endDate: LocalDate) : BlockedAppsEvent
    data object OnBack : BlockedAppsEvent
}
