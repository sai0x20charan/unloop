package com.charan.unloop.presentation.app_block_history

sealed interface AppBlockHistoryEffect {
    data object NavigateBack : AppBlockHistoryEffect
}
