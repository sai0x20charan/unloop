package com.charan.unloop.presentation.session_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.DateTimeUtils.atEndOfDay
import com.charan.unloop.core.utils.DateTimeUtils.atStartOfDay
import com.charan.unloop.data.repository.DataStoreRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.mapper.toSessionSummaryUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@HiltViewModel(assistedFactory = SessionHistoryViewModel.Factory::class)
class SessionHistoryViewModel @AssistedInject constructor(
    private val dataStoreRepository: DataStoreRepository,
    @Assisted("startDate") startDate: LocalDate,
    @Assisted("endDate") endDate: LocalDate,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("startDate") startDate: LocalDate,
            @Assisted("endDate") endDate: LocalDate
        ): SessionHistoryViewModel
    }

    private val _state = MutableStateFlow(SessionHistoryState(dateRange = startDate to endDate))
    val state = _state.asStateFlow()

    private val _effect = Channel<SessionHistoryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            dataStoreRepository.is24Hour.collect { is24Hour ->
                _state.update { it.copy(is24Hour = is24Hour) }
            }
        }
        loadSessions()
    }

    fun onEvent(event: SessionHistoryEvent) {
        when (event) {
            is SessionHistoryEvent.OnDateRangeChange -> {
                val range = event.startDate to event.endDate
                if (_state.value.dateRange != range) {
                    _state.update { it.copy(dateRange = range) }
                    loadSessions()
                }
            }
            SessionHistoryEvent.OnBack -> viewModelScope.launch {
                _effect.send(SessionHistoryEffect.NavigateBack)
            }
            is SessionHistoryEvent.OnSessionDetails -> viewModelScope.launch {
                _effect.send(SessionHistoryEffect.NavigateToSessionDetails(event.id))
            }
        }
    }

    private fun loadSessions() {

        val range = _state.value.dateRange ?: return

       viewModelScope.launch {

                val sessions = sessionRepository.getSessionWithSessionEventForRange(
                    startDate = range.first.atStartOfDay(),
                    endTime = range.second.atEndOfDay()
                ).sortedByDescending { it.sessionEntity.createdAt }
                    .map { it.toSessionSummaryUiModel() }
                _state.update { it.copy(sessions = sessions) }

        }
    }
}
