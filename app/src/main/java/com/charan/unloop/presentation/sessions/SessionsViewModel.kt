package com.charan.unloop.presentation.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.DateTimeUtils.atEndOfDay
import com.charan.unloop.core.utils.DateTimeUtils.atStartOfDay
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

@HiltViewModel(assistedFactory = SessionsViewModel.Factory::class)
class SessionsViewModel @AssistedInject constructor(
    @Assisted("startDate") startDate: LocalDate,
    @Assisted("endDate") endDate: LocalDate,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("startDate") startDate: LocalDate,
            @Assisted("endDate") endDate: LocalDate
        ): SessionsViewModel
    }

    private val _state = MutableStateFlow(SessionsState(dateRange = startDate to endDate))
    val state = _state.asStateFlow()

    private val _effect = Channel<SessionsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadSessions()
    }

    fun onEvent(event: SessionsEvent) {
        when (event) {
            is SessionsEvent.OnDateRangeChange -> {
                val range = event.startDate to event.endDate
                if (_state.value.dateRange != range) {
                    _state.value = SessionsState(dateRange = range)
                    loadSessions()
                }
            }
            SessionsEvent.OnBack -> viewModelScope.launch {
                _effect.send(SessionsEffect.NavigateBack)
            }
            is SessionsEvent.OnSessionDetails -> viewModelScope.launch {
                _effect.send(SessionsEffect.NavigateToSessionDetails(event.id))
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
