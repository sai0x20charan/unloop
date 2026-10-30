package com.charan.unloop.presentation.home

sealed class HomeEvent {
    data class OnAppClicked(val packageName: String) : HomeEvent()
    data class OnHourChange(val hour: String) : HomeEvent()
    data class OnMinuteChange(val minute: String) : HomeEvent()
    data class OnPresetDurationSelect(val preset : Int) : HomeEvent()
    data class OnSessionTitleChange(val title: String) : HomeEvent()

    data object OnInstantSessionCustomizeToggle : HomeEvent()

    data object OnAppsSheetDismiss : HomeEvent()

    data object OnSelectAllApps : HomeEvent()

    data object OnEditSessionTime : HomeEvent()

    data object OnStartSessionClick : HomeEvent()

    data object OnEndSessionClick : HomeEvent()

    data object OnPauseSessionClick : HomeEvent()
    data object OnEditTitleClick : HomeEvent()
    data object OnSessionControlsLockToggle : HomeEvent()

    data object OnWaitTimeToggle : HomeEvent()
    data class OnWaitTimeChange(val time : Int) : HomeEvent()

}
