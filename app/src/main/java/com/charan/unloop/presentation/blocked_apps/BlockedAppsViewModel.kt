package com.charan.unloop.presentation.blocked_apps

import androidx.compose.ui.util.fastFlatMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.DateTimeUtils.atEndOfDay
import com.charan.unloop.core.utils.DateTimeUtils.atStartOfDay
import com.charan.unloop.data.apps.InstalledAppsProvider
import com.charan.unloop.data.local.model.enums.SessionEvents
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.mapper.toBlockedAppsUiModel
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
import kotlinx.datetime.LocalDate

@HiltViewModel(assistedFactory = BlockedAppsViewModel.Factory::class)
class BlockedAppsViewModel @AssistedInject constructor(
    @Assisted("startDate") startDate: LocalDate,
    @Assisted("endDate") endDate: LocalDate,
    private val sessionRepository: SessionRepository,
    private val installedAppsProvider: InstalledAppsProvider,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("startDate") startDate: LocalDate,
            @Assisted("endDate") endDate: LocalDate,
        ): BlockedAppsViewModel
    }

    private val _state = MutableStateFlow(BlockedAppsState(dateRange = startDate to endDate))
    val state = _state.asStateFlow()

    private val _effect = Channel<BlockedAppsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadBlockedApps()
    }

    fun onEvent(event: BlockedAppsEvent) {
        when (event) {
            is BlockedAppsEvent.OnDateRangeChange -> {
                val range = event.startDate to event.endDate
                if (_state.value.dateRange != range) {
                    _state.value = BlockedAppsState(dateRange = range)
                    loadBlockedApps()
                }
            }

            BlockedAppsEvent.OnBack -> viewModelScope.launch {
                _effect.send(BlockedAppsEffect.NavigateBack)
            }
        }
    }

    private fun loadBlockedApps() = viewModelScope.launch{
        val range = _state.value.dateRange ?: return@launch
            val sessions = sessionRepository.getSessionWithSessionEventForRange(
                startDate = range.first.atStartOfDay(),
                endTime = range.second.atEndOfDay(),
            )
        val eventsByPackage = sessions
            .flatMap { it.sessionEvents }
            .groupBy { it.packageName }

        val blockedApps: List<BlockedAppsUiModel> = sessions
            .flatMap { it.sessionEntity.blockedApps.orEmpty() }
            .distinct()
            .map { packageName ->
                val appDetail = installedAppsProvider
                    .getAppInfo(packageName)

                val events = eventsByPackage[packageName].orEmpty()

                appDetail.toBlockedAppsUiModel(
                    packageName = packageName,
                    blockedCount = events.count {
                        it.eventType == SessionEvents.APP_BLOCKED
                    },
                    accessGrantedCount = events.count {
                        it.eventType == SessionEvents.APP_ACCESS_GRANTED
                    }
                )
            }
        _state.update { it.copy(
            blockedApps = blockedApps
        ) }







    }
}
