package com.charan.unloop.presentation.stats

import android.R.attr.timeZone
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.atEndOfDay
import com.charan.unloop.core.utils.DateTimeUtils.atStartOfDay
import com.charan.unloop.core.utils.DateTimeUtils.getNextMonth
import com.charan.unloop.core.utils.DateTimeUtils.getNextWeek
import com.charan.unloop.core.utils.DateTimeUtils.getPreviousMonth
import com.charan.unloop.core.utils.DateTimeUtils.getPreviousWeek
import com.charan.unloop.core.utils.DateTimeUtils.toSeconds
import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.mapper.toFocusChartDataUiModels
import com.charan.unloop.presentation.common.mapper.toSessionSummaryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository
) : ViewModel(){
    private val _state = MutableStateFlow(StatsState())
    val state = _state.asStateFlow()

    private val _effect = Channel<StatsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        init()
        observePeriodChanges()
    }

    private fun init() = viewModelScope.launch {
        _state.update {
            it.copy(
                selectedDateRange = DateTimeUtils.getCurrentWeekPair()
            )
        }
        observeDateRangeChanges()
    }

    fun onEvent(event : StatsEvent){
        when(event){
            StatsEvent.OnSeeAllSessions -> sendEffect(StatsEffect.NavigateToSessions(_state.value.selectedDateRange.first,_state.value.selectedDateRange.second))
            StatsEvent.OnDecrementPeriod -> handleDecrementPeriod()
            StatsEvent.OnIncrementPeriod -> handleIncrementPeriod()
            is StatsEvent.OnPeriodChange -> handlePeriodChange()
            is StatsEvent.OnSessionDetails -> sendEffect(StatsEffect.NavigateToSessionDetails(event.id))
        }
    }

    private fun sendEffect(effect : StatsEffect) = viewModelScope.launch {
        _effect.send(effect)
    }

    private fun handleDecrementPeriod() {
        val period = _state.value.selectedPeriod
        if(period == StatsPeriod.WEEKLY){
            _state.update {
                it.copy(
                    selectedDateRange = _state.value.selectedDateRange.getPreviousWeek()
                )
            }
        }
        if(period == StatsPeriod.MONTHLY){
            _state.update {
                it.copy(
                    selectedDateRange = _state.value.selectedDateRange.getPreviousMonth()
                )
            }
        }



    }

    private fun handleIncrementPeriod() {
        val period = _state.value.selectedPeriod
        if(period == StatsPeriod.WEEKLY){
            _state.update {
                it.copy(
                    selectedDateRange = _state.value.selectedDateRange.getNextWeek()
                )
            }
        }
        if(period == StatsPeriod.MONTHLY){
            _state.update {
                it.copy(
                    selectedDateRange = _state.value.selectedDateRange.getNextMonth()
                )
            }
        }

    }

    private fun handlePeriodChange() {
        _state.update {
            it.copy(
                selectedPeriod = if(_state.value.selectedPeriod == StatsPeriod.WEEKLY) StatsPeriod.MONTHLY else StatsPeriod.WEEKLY,

            )
        }

    }

    private fun observePeriodChanges() = viewModelScope.launch {
        _state
            .map { it.selectedPeriod }
            .distinctUntilChanged()
            .collectLatest { period ->

                if(period == StatsPeriod.WEEKLY){
                    _state.update {
                        it.copy(
                            selectedDateRange = DateTimeUtils.getCurrentWeekPair()
                        )
                    }
                }

                if(period == StatsPeriod.MONTHLY){
                    _state.update {
                        it.copy(
                            selectedDateRange = DateTimeUtils.getCurrentMonthPair()
                        )
                    }
                }
            }
    }

    private fun observeDateRangeChanges() = viewModelScope.launch {
        _state
            .map { it.selectedDateRange }
            .distinctUntilChanged()
            .collectLatest { range ->
                val startTime = range.first.atStartOfDay()
                val endTime = range.second.atEndOfDay()

                sessionRepository.getSessionWithSessionEventForRange(startTime,endTime).let { sessions->
                    val focusChartData = sessions.toFocusChartDataUiModels(
                        range = range,
                        currentDateTime = DateTimeUtils.getCurrentDateTime()
                    )
                    _state.update {
                        it.copy(
                            focusChartData = focusChartData,
                            totalFocusDurationSeconds = focusChartData.sumOf { it.focusDurationSeconds },
                            sessionCount = sessions.size,
                            appBlockCount = sessions.sumOf { it.sessionEvents.count { event -> event.eventType == SessionEvents.APP_BLOCKED } },
                            recentSessions = sessions.sortedByDescending { it.sessionEntity.createdAt }.take(5).map { session ->
                                session.toSessionSummaryUiModel()
                            },

                        )
                    }



                }



            }



    }






}
