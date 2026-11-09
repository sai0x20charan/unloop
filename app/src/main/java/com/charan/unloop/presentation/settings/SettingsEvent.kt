package com.charan.unloop.presentation.settings

import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.data.model.TimeFormatOptions

sealed interface SettingsEvent {
    data class OnTimeFormatChange(val format: TimeFormatOptions) : SettingsEvent
    data class OnThemeChange(val theme: ThemeOptions) : SettingsEvent
    data class OnDynamicColorsChange(val isEnabled: Boolean) : SettingsEvent
    data object OnBack : SettingsEvent

    data object OnSendFeedbackClick : SettingsEvent
    data object OnRateAppClick : SettingsEvent
    data object OnOpenSourceCodeClick : SettingsEvent

    data object OnOpenSourceLicensesClick : SettingsEvent
}
