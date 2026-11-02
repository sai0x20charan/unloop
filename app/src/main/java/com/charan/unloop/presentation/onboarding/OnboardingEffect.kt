package com.charan.unloop.presentation.onboarding

sealed class OnboardingEffect {
    object RequestNotificationPermission : OnboardingEffect()
    object Complete : OnboardingEffect()

    data class ScrollToPage(val page: Int) : OnboardingEffect()

}
