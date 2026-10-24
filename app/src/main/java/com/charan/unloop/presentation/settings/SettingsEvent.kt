package com.charan.unloop.presentation.settings

import com.charan.unloop.data.model.ThemeOptions

sealed interface SettingsEvent {
    data class OnThemeChange(val theme: ThemeOptions) : SettingsEvent
    data class OnDynamicColorsChange(val isEnabled: Boolean) : SettingsEvent
    data object OnBack : SettingsEvent
    data object OnCheckPermissions : SettingsEvent
    data object OnRequestExactAlarmPermission : SettingsEvent
}
