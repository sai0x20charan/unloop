package com.charan.unloop.presentation.on_boarding

sealed class OnBoardingEffect {
    object RequestNotificationPermission : OnBoardingEffect()
    object Complete : OnBoardingEffect()

    data class ScrollToPage(val page: Int) : OnBoardingEffect()

}
