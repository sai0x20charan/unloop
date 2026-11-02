package com.charan.unloop.presentation.common.mapper

import com.charan.unloop.data.model.AppInfo
import com.charan.unloop.presentation.common.model.BlockedAppsUiModel
import com.charan.unloop.presentation.common.models.AppInfoUiModel

fun AppInfo.toAppInfoUiModel(isSelected: Boolean = false) = AppInfoUiModel(
    appName = appName,
    packageName = packageName,
    icon = icon,
    isSelected = isSelected
)

fun AppInfo?.toBlockedAppsUiModel(
    packageName: String,
    blockedCount: Int = 0,
    accessGrantedCount: Int = 0
) = BlockedAppsUiModel(
    appName = this?.appName ?: packageName,
    appPackage = this?.packageName ?: packageName,
    appIcon = this?.icon,
    blockedCount = blockedCount,
    accessGrantedCount = accessGrantedCount
)
