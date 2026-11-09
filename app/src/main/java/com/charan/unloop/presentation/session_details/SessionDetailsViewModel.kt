package com.charan.unloop.presentation.session_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.apps.InstalledAppsProvider
import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.data.repository.DataStoreRepository
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.mapper.toBlockedAppUiModel
import com.charan.unloop.presentation.common.mapper.toFocusIntervals
import com.charan.unloop.presentation.common.mapper.toFocusSegmentUiModels
import com.charan.unloop.presentation.common.mapper.toPauseIntervals
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

@HiltViewModel(assistedFactory = SessionDetailsViewModel.Factory::class)
class SessionDetailsViewModel @AssistedInject constructor(
    private val dataStoreRepository: DataStoreRepository,
    @Assisted("sessionId") private val sessionId: String,
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val installedAppsProvider: InstalledAppsProvider
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("sessionId") sessionId: String
        ): SessionDetailsViewModel
    }

    private val _state = MutableStateFlow(SessionDetailsState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SessionDetailsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            dataStoreRepository.is24Hour.collect { is24Hour ->
                _state.update { it.copy(is24Hour = is24Hour) }
            }
        }
        loadSessionDetails()
    }

    fun onEvent(event: SessionDetailsEvent) {
        when (event) {
            SessionDetailsEvent.OnBack -> viewModelScope.launch {
                _effect.send(SessionDetailsEffect.NavigateBack)
            }
        }
    }

    private fun loadSessionDetails() = viewModelScope.launch {
        val session = sessionRepository.getSessionWithSessionEventById(sessionId)
        val totalFocusIntervals = session.toFocusIntervals()
        val totalFocusDurationSeconds = DateTimeUtils.getIntervalDuration(totalFocusIntervals).inWholeSeconds.toInt()
        val pauseEvents = session.sessionEvents.filter { it.eventType == SessionEventType.PAUSE_SESSION }
        val pausedDurationSeconds = DateTimeUtils.getIntervalDuration(session.toPauseIntervals()).inWholeSeconds.toInt()
        val appAccessGrantedCount = session.sessionEvents.filter { it.eventType == SessionEventType.APP_ACCESS_GRANTED }.size
        val focusSegments = totalFocusIntervals.toFocusSegmentUiModels(
            startTime = session.sessionEntity.createdAt,
            endTime = session.sessionEntity.scheduledEndTime,
            actualEndTime = session.sessionEntity.actualEndTime,
        )

        val blockedApps = session.sessionEntity.blockedAppPackages?.map { app->
            val appDetails = installedAppsProvider.getAppInfo(app)
            val blockedCount = session.sessionEvents.filter { it.packageName == app && it.eventType == SessionEventType.APP_BLOCKED }.size
            val unlockCount = session.sessionEvents.filter { it.packageName == app && it.eventType == SessionEventType.APP_ACCESS_GRANTED }.size
            appDetails.toBlockedAppUiModel(
                packageName = app,
                blockedCount = blockedCount,
                accessGrantedCount = unlockCount
            )
        } ?: emptyList()

        _state.update {
            it.copy(
                totalFocusDurationSeconds = totalFocusDurationSeconds,
                focusSegments = focusSegments,
                blockedApps = blockedApps,
                sessionName = session.sessionEntity.name,
                sessionType = session.sessionEntity.type,
                createdAt = session.sessionEntity.createdAt,
                endTime = session.sessionEntity.scheduledEndTime,
                actualEndTime = session.sessionEntity.actualEndTime,
                pauseCount = pauseEvents.size,
                totalPausedDurationSeconds = pausedDurationSeconds,
                totalSessionDurationSeconds = DateTimeUtils.getDurationSeconds(session.sessionEntity.createdAt, session.sessionEntity.scheduledEndTime).toInt(),
                appAccessGrantedCount = appAccessGrantedCount
            )
        }
    }
}
