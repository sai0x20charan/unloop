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
import com.charan.unloop.presentation.common.mapper.toSessions
import com.charan.unloop.presentation.common.mapper.toStatsGraphData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository
) : ViewModel(){
    private val _state = MutableStateFlow(StatsState())
    val state = _state.asStateFlow()

    init {
        init()
        observeStatTypeChange()
    }

    private fun init() = viewModelScope.launch {
        _state.update {
            it.copy(
                graphTimePeriod = DateTimeUtils.getCurrentWeekPair()
            )
        }
        observeDateRangeChanges()
    }

    fun onEvent(event : StatsEvent){
        when(event){
            StatsEvent.OnDecrementPeriod -> handleDecrementPeriod()
            StatsEvent.OnIncrementPeriod -> handleIncrementPeriod()
            is StatsEvent.OnStatTypeChange -> handleStatTypeChange()
        }
    }

    private fun handleDecrementPeriod() {
        val statType = _state.value.selectedStatsType
        if(statType == StatsType.WEEKLY){
            _state.update {
                it.copy(
                    graphTimePeriod = _state.value.graphTimePeriod.getPreviousWeek()
                )
            }
        }
        if(statType == StatsType.MONTHLY){
            _state.update {
                it.copy(
                    graphTimePeriod = _state.value.graphTimePeriod.getPreviousMonth()
                )
            }
        }



    }

    private fun handleIncrementPeriod() {
        val statType = _state.value.selectedStatsType
        if(statType == StatsType.WEEKLY){
            _state.update {
                it.copy(
                    graphTimePeriod = _state.value.graphTimePeriod.getNextWeek()
                )
            }
        }
        if(statType == StatsType.MONTHLY){
            _state.update {
                it.copy(
                    graphTimePeriod = _state.value.graphTimePeriod.getNextMonth()
                )
            }
        }

    }

    private fun handleStatTypeChange() {
        _state.update {
            it.copy(
                selectedStatsType = if(_state.value.selectedStatsType == StatsType.WEEKLY) StatsType.MONTHLY else StatsType.WEEKLY,

            )
        }

    }

    private fun observeStatTypeChange() = viewModelScope.launch {
        _state
            .map { it.selectedStatsType }
            .distinctUntilChanged()
            .collectLatest { statType ->

                if(statType == StatsType.WEEKLY){
                    _state.update {
                        it.copy(
                            graphTimePeriod = DateTimeUtils.getCurrentWeekPair()
                        )
                    }
                }

                if(statType == StatsType.MONTHLY){
                    _state.update {
                        it.copy(
                            graphTimePeriod = DateTimeUtils.getCurrentMonthPair()
                        )
                    }
                }
            }
    }

    private fun observeDateRangeChanges() = viewModelScope.launch {
        _state
            .map { it.graphTimePeriod }
            .distinctUntilChanged()
            .collectLatest { range ->
                val startTime = range.first.atStartOfDay()
                val endTime = range.second.atEndOfDay()

                sessionRepository.getSessionWithSessionEventForRange(startTime,endTime).let { sessions->
                    val graphData = sessions.toStatsGraphData(
                        range = range,
                        currentDateTime = DateTimeUtils.getCurrentDateTime()
                    )
                    _state.update {
                        it.copy(
                            graphData = graphData,
                            totalFocusTime = graphData.sumOf { it.value },
                            totalSessions = sessions.size,
                            totalBlockedApps = sessions.sumOf { it.sessionEvents.count { event -> event.eventType == SessionEvents.APP_BLOCKED } },
                            recentSessions = sessions.sortedByDescending { it.sessionEntity.createdAt }.take(5).map { session ->
                                session.toSessions()
                            },

                        )
                    }



                }



            }



    }






}
