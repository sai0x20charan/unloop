package com.charan.unloop.presentation.common.models

import android.graphics.drawable.Drawable

data class InstalledAppsUiModel(
    val appName : String = "",
    val packageName : String = "",
    val icon : Drawable? = null,
    val isSelected : Boolean = false
)