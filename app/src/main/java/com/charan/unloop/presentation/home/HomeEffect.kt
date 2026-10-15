package com.charan.unloop.presentation.home

sealed class HomeEffect {
    object NavigateToAddRoutine : HomeEffect()
    data class ShowToast(val message: String) : HomeEffect()
}