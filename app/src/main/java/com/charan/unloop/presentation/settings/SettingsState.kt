package com.charan.unloop.presentation.settings

import com.charan.unloop.data.model.ThemeOptions

data class SettingsState(
    val theme: ThemeOptions = ThemeOptions.SYSTEM_DEFAULT,
    val isDynamicColorsEnabled: Boolean = true,
    val appVersion : String = ""
)
