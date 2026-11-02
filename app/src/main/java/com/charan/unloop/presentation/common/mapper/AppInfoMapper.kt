package com.charan.unloop.presentation.common.mapper

import com.charan.unloop.data.model.AppInfo
import com.charan.unloop.presentation.common.model.BlockedAppUiModel
import com.charan.unloop.presentation.common.model.AppInfoUiModel

fun AppInfo.toAppInfoUiModel(isSelected: Boolean = false) = AppInfoUiModel(
    appName = appName,
    packageName = packageName,
    icon = icon,
    isSelected = isSelected
)

fun AppInfo?.toBlockedAppUiModel(
    packageName: String,
    blockedCount: Int = 0,
    accessGrantedCount: Int = 0
) = BlockedAppUiModel(
    appName = this?.appName ?: packageName,
    packageName = this?.packageName ?: packageName,
    appIcon = this?.icon,
    blockedCount = blockedCount,
    accessGrantedCount = accessGrantedCount
)
