package com.charan.unloop.presentation.app_block_history

import androidx.compose.ui.util.fastFlatMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charan.unloop.core.utils.DateTimeUtils.atEndOfDay
import com.charan.unloop.core.utils.DateTimeUtils.atStartOfDay
import com.charan.unloop.data.apps.InstalledAppsProvider
import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.data.repository.DataStoreRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.presentation.common.mapper.toBlockedAppUiModel
import com.charan.unloop.presentation.common.model.BlockedAppUiModel
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

@HiltViewModel(assistedFactory = AppBlockHistoryViewModel.Factory::class)
class AppBlockHistoryViewModel @AssistedInject constructor(
    private val dataStoreRepository: DataStoreRepository,
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
        ): AppBlockHistoryViewModel
    }

    private val _state = MutableStateFlow(AppBlockHistoryState(dateRange = startDate to endDate))
    val state = _state.asStateFlow()

    private val _effect = Channel<AppBlockHistoryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            dataStoreRepository.is24Hour.collect { is24Hour ->
                _state.update { it.copy(is24Hour = is24Hour) }
            }
        }
        loadBlockedApps()
    }

    fun onEvent(event: AppBlockHistoryEvent) {
        when (event) {
            is AppBlockHistoryEvent.OnDateRangeChange -> {
                val range = event.startDate to event.endDate
                if (_state.value.dateRange != range) {
                    _state.update { it.copy(dateRange = range) }
                    loadBlockedApps()
                }
            }

            AppBlockHistoryEvent.OnBack -> viewModelScope.launch {
                _effect.send(AppBlockHistoryEffect.NavigateBack)
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

        val blockedApps: List<BlockedAppUiModel> = sessions
            .flatMap { it.sessionEntity.blockedAppPackages.orEmpty() }
            .distinct()
            .map { packageName ->
                val appDetail = installedAppsProvider
                    .getAppInfo(packageName)

                val events = eventsByPackage[packageName].orEmpty()

                appDetail.toBlockedAppUiModel(
                    packageName = packageName,
                    blockedCount = events.count {
                        it.eventType == SessionEventType.APP_BLOCKED
                    },
                    accessGrantedCount = events.count {
                        it.eventType == SessionEventType.APP_ACCESS_GRANTED
                    }
                )
            }
        _state.update { it.copy(
            blockedApps = blockedApps
        ) }







    }
}
