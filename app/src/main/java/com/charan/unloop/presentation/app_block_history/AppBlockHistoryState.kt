package com.charan.unloop.presentation.app_block_history

import com.charan.unloop.presentation.common.model.BlockedAppUiModel
import kotlinx.datetime.LocalDate

data class AppBlockHistoryState(
    val is24Hour: Boolean = false,
    val dateRange: Pair<LocalDate, LocalDate>? = null,
    val blockedApps: List<BlockedAppUiModel> = emptyList(),

)
