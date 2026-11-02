package com.charan.unloop.presentation.onboarding

sealed class OnboardingEvent {
    data class OnRequestNotificationPermission(val showRationale: Boolean) : OnboardingEvent()
    object OnRequestDisplayOverAppsPermission : OnboardingEvent()
    object OnRequestUsageStatsPermission : OnboardingEvent()
    object OnCheckAllPermissionStatus : OnboardingEvent()
    object OnComplete : OnboardingEvent()

    data class OnPageChange(val pageIndex : Int) : OnboardingEvent()

    object OnNextPage : OnboardingEvent()

    object OnPreviousPage : OnboardingEvent()
}
