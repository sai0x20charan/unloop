package com.charan.unloop.presentation.common.model

import android.graphics.drawable.Drawable

data class BlockedAppsUiModel(
    val appName: String,
    val appPackage: String,
    val appIcon: Drawable? = null,
    val accessGrantedCount: Int = 0,
    val blockedCount: Int = 0,
    val sessionsBlockingAppCount : Int = 0
)
