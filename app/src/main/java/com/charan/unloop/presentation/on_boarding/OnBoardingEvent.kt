package com.charan.unloop.presentation.on_boarding

sealed class OnBoardingEvent {
    data class OnRequestNotificationPermission(val showRationale: Boolean) : OnBoardingEvent()
    object OnRequestDisplayOverAppsPermission : OnBoardingEvent()
    object OnRequestUsageStatsPermission : OnBoardingEvent()
    object OnCheckAllPermissionStatus : OnBoardingEvent()
    object OnComplete : OnBoardingEvent()

    data class OnPageChange(val pageIndex : Int) : OnBoardingEvent()

    object OnNextPage : OnBoardingEvent()

    object OnPreviousPage : OnBoardingEvent()
}
