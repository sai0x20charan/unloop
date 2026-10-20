package com.charan.unloop.presentation.root

import com.charan.unloop.data.model.ThemeOptions

data class AppState(
    val isLoading: Boolean = true,
    val isOnBoardingCompleted: Boolean = true,
    val theme: ThemeOptions = ThemeOptions.SYSTEM_DEFAULT,
    val dynamicColorsEnabled: Boolean = true
)
