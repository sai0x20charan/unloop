package com.charan.unloop.presentation.session_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.data.repository.InstalledAppsRepository
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.mapper.toFocusIntervals
import com.charan.unloop.presentation.common.mapper.toFocusSegmentUiModels
import com.charan.unloop.presentation.common.mapper.toPauseIntervals
import com.charan.unloop.presentation.common.model.BlockedAppsUiModel
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
    @Assisted("sessionId") private val sessionId: String,
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val installedAppsRepository: InstalledAppsRepository
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
        val totalFocusTime = DateTimeUtils.getIntervalDuration(totalFocusIntervals).inWholeSeconds.toInt()
        val pausedSessions = session.sessionEvents.filter { it.eventType == SessionEvents.PAUSE_SESSION }
        val pausedTotalTime = DateTimeUtils.getIntervalDuration(session.toPauseIntervals()).inWholeSeconds.toInt()
        val appAccessGrantedCount = session.sessionEvents.filter { it.eventType == SessionEvents.APP_ACCESS_GRANTED }.size
        val focusSegments = totalFocusIntervals.toFocusSegmentUiModels(
            startTime = session.sessionEntity.createdAt,
            endTime = session.sessionEntity.endTime,
            actualEndTime = session.sessionEntity.actualEndTime,
        )

        val blockedApps = session.sessionEntity.blockedApps?.map { app->
            val appDetails = installedAppsRepository.getAppDetailsByPackageName(app)
            val blockedCount = session.sessionEvents.filter { it.packageName == app && it.eventType == SessionEvents.APP_BLOCKED }.size
            val unlockCount = session.sessionEvents.filter { it.packageName == app && it.eventType == SessionEvents.APP_ACCESS_GRANTED }.size
            BlockedAppsUiModel(
                appName = appDetails.appName,
                appIcon = appDetails.icon,
                appPackage = app,
                blockedCount = blockedCount,
                accessGrantedCount = unlockCount
            )
        } ?: emptyList()

        _state.update {
            it.copy(
                totalFocusTime = totalFocusTime,
                focusSegments = focusSegments,
                blockedApps = blockedApps,
                sessionName = session.sessionEntity.sessionName,
                sessionType = session.sessionEntity.sessionType,
                createdAt = session.sessionEntity.createdAt,
                endTime = session.sessionEntity.endTime,
                actualEndTime = session.sessionEntity.actualEndTime,
                totalPausedSessions = pausedSessions.size,
                totalPausedTime = pausedTotalTime,
                totalSessionDuration = DateTimeUtils.getDurationSeconds(session.sessionEntity.createdAt, session.sessionEntity.endTime).toInt(),
                appAccessGrantedCount = appAccessGrantedCount
            )
        }
    }
}
