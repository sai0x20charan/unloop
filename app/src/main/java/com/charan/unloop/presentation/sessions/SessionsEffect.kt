package com.charan.unloop.presentation.sessions

sealed interface SessionsEffect {
    data object NavigateBack : SessionsEffect
}
