package com.charan.unloop.presentation.root

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.presentation.navigation.RootNavigation

@Composable
fun AppRoot() {
    val viewModel = hiltViewModel<AppViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface {
        RootNavigation(
            isOnBoardingCompleted = state.isOnBoardingCompleted
        )
    }
}