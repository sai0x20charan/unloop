package com.charan.unloop.presentation.session_details

sealed interface SessionDetailsEffect {
    data object NavigateBack : SessionDetailsEffect
}
