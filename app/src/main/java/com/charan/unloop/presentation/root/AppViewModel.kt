package com.charan.unloop.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AppViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository

) : ViewModel(){

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    init {
        checkPreferences()
    }
    fun checkPreferences() = viewModelScope.launch {
        _state.update {

            it.copy(
                isOnBoardingCompleted = dataStoreRepository.isOnBoardingCompleted.first()
            )
        }
    }

}