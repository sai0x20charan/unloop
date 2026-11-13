package com.charan.unloop.presentation.settings

import com.charan.unloop.presentation.common.ToastMessage

sealed interface SettingsEffect {
    data object OnBack : SettingsEffect

    data object LaunchSendFeedbackEmail : SettingsEffect
    data class OpenUrl(val url: String) : SettingsEffect

    data object OnNavigateToOpenSourceLicenses : SettingsEffect

    data class SelectBackupDestination(val suggestedBackupFileName: String) : SettingsEffect

    data class ShowToast(val message: ToastMessage) : SettingsEffect
    data object SelectBackupFile : SettingsEffect
}
