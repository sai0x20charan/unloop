package com.charan.unloop.presentation.onboarding

data class OnboardingState(
    val isNotificationPermissionGranted : Boolean = false,
    val isDisplayOverAppsPermissionGranted : Boolean = false,
    val isUsageStatsPermissionGranted : Boolean = false,
    val totalPages : Int = 2,
    val currentPageIndex : Int = 0

) {
    val allPermissionsGranted: Boolean
        get() = isNotificationPermissionGranted &&
            isDisplayOverAppsPermissionGranted && isUsageStatsPermissionGranted
}
