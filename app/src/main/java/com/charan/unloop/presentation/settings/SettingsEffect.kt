package com.charan.unloop.presentation.settings

sealed interface SettingsEffect {
    data object OnBack : SettingsEffect

    data object LaunchSendFeedbackEmail : SettingsEffect
    data class OpenUrl(val url: String) : SettingsEffect

    data object OnNavigateToOpenSourceLicenses : SettingsEffect
}
