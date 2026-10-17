package com.charan.unloop.presentation.common.models

import android.graphics.drawable.Drawable
import com.charan.unloop.data.model.InstalledApp

data class InstalledAppsUiModel(
    val appName: String = "",
    val packageName: String = "",
    val icon: Drawable? = null,
    val isSelected: Boolean = false
)

fun InstalledApp.toInstalledAppsUiModel(isSelected: Boolean = false) = InstalledAppsUiModel(
    appName = appName,
    packageName = packageName,
    icon = icon,
    isSelected = isSelected
)

fun List<InstalledAppsUiModel>.toggleAppSelection(packageName: String): List<InstalledAppsUiModel> =
    map { app ->
        if (app.packageName == packageName) app.copy(isSelected = !app.isSelected) else app
    }

fun List<InstalledAppsUiModel>.toggleSelectAll(): List<InstalledAppsUiModel> {
    val allSelected = all { it.isSelected }
    return map { it.copy(isSelected = !allSelected) }
}
