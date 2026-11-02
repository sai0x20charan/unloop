package com.charan.unloop.presentation.blocked_apps

import com.charan.unloop.presentation.common.model.BlockedAppsUiModel
import kotlinx.datetime.LocalDate

data class BlockedAppsState(
    val dateRange: Pair<LocalDate, LocalDate>? = null,
    val blockedApps: List<BlockedAppsUiModel> = emptyList(),

)
