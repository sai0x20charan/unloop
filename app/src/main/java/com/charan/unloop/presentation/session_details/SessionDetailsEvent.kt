package com.charan.unloop.presentation.session_details

sealed interface SessionDetailsEvent {
    data object OnBack : SessionDetailsEvent
}
