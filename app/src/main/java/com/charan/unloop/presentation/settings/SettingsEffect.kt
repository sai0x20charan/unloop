package com.charan.unloop.presentation.settings

sealed interface SettingsEffect {
    data object OnBack : SettingsEffect

    data object OnNavigateToOpenSourceLicenses : SettingsEffect
}
