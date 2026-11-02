package com.charan.unloop.presentation.common.models

import android.graphics.drawable.Drawable

data class AppInfoUiModel(
    val appName: String = "",
    val packageName: String = "",
    val icon: Drawable? = null,
    val isSelected: Boolean = false
)

fun List<AppInfoUiModel>.toggleAppSelection(packageName: String): List<AppInfoUiModel> =
    map { app ->
        if (app.packageName == packageName) app.copy(isSelected = !app.isSelected) else app
    }

fun List<AppInfoUiModel>.toggleSelectAll(): List<AppInfoUiModel> {
    val allSelected = all { it.isSelected }
    return map { it.copy(isSelected = !allSelected) }
}
