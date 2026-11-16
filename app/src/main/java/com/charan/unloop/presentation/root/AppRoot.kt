package com.charan.unloop.presentation.root

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
            ProvideAppHaptics(enabled = state.isHapticsEnabled) {
                RootNavigation(
                    isOnboardingCompleted = state.isOnboardingCompleted
                )
            }
        }
    }
}

@Composable
fun ProvideAppHaptics(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val systemHaptics = LocalHapticFeedback.current

    val appHaptics = remember(enabled, systemHaptics) {
        object : HapticFeedback {
            override fun performHapticFeedback(
                hapticFeedbackType: HapticFeedbackType
            ) {
                if (enabled) {
                    systemHaptics.performHapticFeedback(hapticFeedbackType)
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalHapticFeedback provides appHaptics,
        content = content
    )
}

