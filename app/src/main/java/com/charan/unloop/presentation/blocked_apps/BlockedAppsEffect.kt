package com.charan.unloop.presentation.blocked_apps

sealed interface BlockedAppsEffect {
    data object NavigateBack : BlockedAppsEffect
}
