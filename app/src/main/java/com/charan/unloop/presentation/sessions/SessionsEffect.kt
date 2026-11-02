package com.charan.unloop.presentation.sessions

sealed interface SessionsEffect {
    data object NavigateBack : SessionsEffect
    data class NavigateToSessionDetails(val id: String) : SessionsEffect
}
