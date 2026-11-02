package com.charan.unloop.presentation.session_details

import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.presentation.common.model.BlockedAppUiModel
import com.charan.unloop.presentation.common.model.FocusSegmentUiModel
import kotlinx.datetime.LocalDateTime

data class SessionDetailsState(
    val totalFocusDurationSeconds: Int = 0,
    val endTime : LocalDateTime = DateTimeUtils.getCurrentDateTime(),
    val actualEndTime: LocalDateTime? = null,
    val blockedApps: List<BlockedAppUiModel> = emptyList(),
    val focusSegments: List<FocusSegmentUiModel> = emptyList(),
    val sessionName: String = "",
    val sessionType: SessionType = SessionType.INSTANT,
    val createdAt: LocalDateTime = DateTimeUtils.getCurrentDateTime(),
    val totalPausedDurationSeconds : Int = 0,
    val pauseCount : Int = 0,
    val totalSessionDurationSeconds : Int = 0,
    val appAccessGrantedCount : Int = 0
)
