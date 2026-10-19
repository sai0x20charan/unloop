package com.charan.unloop.presentation.on_boarding

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
class OnBoardingViewModel @Inject constructor(

    private val permissionManager: PermissionManager,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnBoardingState())

    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<OnBoardingEffect>()
    val effect = _effect.asSharedFlow()

    init {
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

    fun onEvent(event: OnBoardingEvent){
        when(event){
            is OnBoardingEvent.OnRequestNotificationPermission -> {
                handleNotificationPermission(event.showRationale)


            }
            is OnBoardingEvent.OnRequestDisplayOverAppsPermission -> {
                permissionManager.requestOverlayPermission()
            }
            is OnBoardingEvent.OnRequestUsageStatsPermission -> {
                permissionManager.requestUsageStatsPermission()
            }

            OnBoardingEvent.OnCheckAllPermissionStatus -> {
                checkAllPermissionStatus()
            }

            OnBoardingEvent.OnComplete -> {
                if (state.value.allPermissionsGranted) sendEffect(OnBoardingEffect.Complete)
            }

            OnBoardingEvent.OnNextPage -> {
                handleNextPageChange()

            }
            OnBoardingEvent.OnPreviousPage -> handlePreviousPageChange()
            is OnBoardingEvent.OnPageChange -> handlePageChange(event.pageIndex)
        }
    }

    private fun handleNextPageChange() = viewModelScope.launch {
        if(_state.value.currentPageIndex == _state.value.totalPages - 1){
            dataStoreRepository.setOnBoardingCompleted(true)

            sendEffect(OnBoardingEffect.Complete)
        } else{
            sendEffect(OnBoardingEffect.ScrollToPage(_state.value.currentPageIndex + 1))
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
            sendEffect(OnBoardingEffect.RequestNotificationPermission)

        }
    }

    private fun sendEffect(effect : OnBoardingEffect) = viewModelScope.launch {
        _effect.emit(effect)
    }




}
