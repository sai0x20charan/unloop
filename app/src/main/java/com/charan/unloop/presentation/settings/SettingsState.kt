package com.charan.unloop.presentation.settings

import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.data.model.TimeFormatOptions

data class SettingsState(
    val is24Hour: Boolean = false,
    val timeFormat: TimeFormatOptions = TimeFormatOptions.SYSTEM_DEFAULT,
    val theme: ThemeOptions = ThemeOptions.SYSTEM_DEFAULT,
    val isDynamicColorsEnabled: Boolean = true,
    val appVersion : String = ""
)
