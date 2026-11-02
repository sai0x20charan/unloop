package com.charan.unloop.presentation.session_history

sealed interface SessionHistoryEffect {
    data object NavigateBack : SessionHistoryEffect
    data class NavigateToSessionDetails(val id: String) : SessionHistoryEffect
}
