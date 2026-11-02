package com.charan.unloop.presentation.session_details

import android.graphics.drawable.Drawable
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.presentation.common.model.FocusSegmentUiModel
import kotlinx.datetime.LocalDateTime

data class SessionDetailsState(
    val totalFocusTime: Int = 0,
    val endTime : LocalDateTime = DateTimeUtils.getCurrentDateTime(),
    val actualEndTime: LocalDateTime? = null,
    val blockedApps: List<BlockedAppsUiModel> = emptyList(),
    val focusSegments: List<FocusSegmentUiModel> = emptyList(),
    val sessionName: String = "",
    val sessionType: SessionType = SessionType.INSTANT,
    val createdAt: LocalDateTime = DateTimeUtils.getCurrentDateTime(),
    val totalPausedTime : Int = 0,
    val totalPausedSessions : Int = 0,
    val totalSessionDuration : Int = 0,
    val appAccessGrantedCount : Int = 0
)

data class BlockedAppsUiModel(
    val appName: String,
    val appPackage: String,
    val appIcon: Drawable? = null,
    val accessGrantedCount: Int = 0,
    val blockedCount: Int = 0
)
