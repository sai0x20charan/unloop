package com.charan.unloop.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.PermissionManager
import com.charan.unloop.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class OnboardingViewModel @Inject constructor(

    private val permissionManager: PermissionManager,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())

    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<OnboardingEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            dataStoreRepository.is24Hour.collect { is24Hour ->
                _state.update { it.copy(is24Hour = is24Hour) }
            }
        }
        checkAllPermissionStatus()
    }


    private fun checkAllPermissionStatus(){
        _state.update {
            it.copy(
                isNotificationPermissionGranted = permissionManager.isNotificationPermissionGranted,
                isDisplayOverAppsPermissionGranted = permissionManager.hasOverlayPermission(),
                isUsageStatsPermissionGranted = permissionManager.hasUsageStatsPermission()
            )
        }
    }

    fun onEvent(event: OnboardingEvent){
        when(event){
            is OnboardingEvent.OnRequestNotificationPermission -> {
                handleNotificationPermission(event.showRationale)


            }
            is OnboardingEvent.OnRequestDisplayOverAppsPermission -> {
                permissionManager.requestOverlayPermission()
            }
            is OnboardingEvent.OnRequestUsageStatsPermission -> {
                permissionManager.requestUsageStatsPermission()
            }

            OnboardingEvent.OnCheckAllPermissionStatus -> {
                checkAllPermissionStatus()
            }

            OnboardingEvent.OnComplete -> {
                if (state.value.allPermissionsGranted) sendEffect(OnboardingEffect.Complete)
            }

            OnboardingEvent.OnNextPage -> {
                handleNextPageChange()

            }
            OnboardingEvent.OnPreviousPage -> handlePreviousPageChange()
            is OnboardingEvent.OnPageChange -> handlePageChange(event.pageIndex)
        }
    }

    private fun handleNextPageChange() = viewModelScope.launch {
        if(_state.value.currentPageIndex == _state.value.totalPages - 1){
            dataStoreRepository.setOnboardingCompleted(true)

            sendEffect(OnboardingEffect.Complete)
        } else{
            sendEffect(OnboardingEffect.ScrollToPage(_state.value.currentPageIndex + 1))
            _state.update {
                it.copy(
                    currentPageIndex = it.currentPageIndex + 1
                )
            }
        }
    }

    private fun handlePreviousPageChange() = viewModelScope.launch {

            _state.update {
                it.copy(
                    currentPageIndex = it.currentPageIndex - 1
                )
            }

    }

    private fun handlePageChange(pageIndex : Int) {
        _state.update {
            it.copy(
                currentPageIndex = pageIndex
            )
        }
    }

    private fun handleNotificationPermission(showRationale: Boolean){
        if(showRationale){
            permissionManager.openSettingsPermissionScreen()
        } else{
            sendEffect(OnboardingEffect.RequestNotificationPermission)

        }
    }

    private fun sendEffect(effect : OnboardingEffect) = viewModelScope.launch {
        _effect.emit(effect)
    }




}
