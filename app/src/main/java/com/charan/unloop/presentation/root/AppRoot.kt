package com.charan.unloop.presentation.root

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.data.model.ThemeOptions
import com.charan.unloop.presentation.navigation.RootNavigation
import com.charan.unloop.presentation.theme.UnloopTheme

@Composable
fun AppRoot(
    viewModel: AppViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isDarkMode = when (state.theme) {
        ThemeOptions.DARK -> true
        ThemeOptions.LIGHT -> false
        ThemeOptions.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    UnloopTheme(
        darkTheme = isDarkMode,
        dynamicColor = state.dynamicColorsEnabled
    ) {
        Surface {
            RootNavigation(
                isOnboardingCompleted = state.isOnboardingCompleted
            )
        }
    }
}
