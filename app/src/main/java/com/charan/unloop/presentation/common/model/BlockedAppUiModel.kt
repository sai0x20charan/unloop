package com.charan.unloop.presentation.common.model

import android.graphics.drawable.Drawable

data class BlockedAppUiModel(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable? = null,
    val accessGrantedCount: Int = 0,
    val blockedCount: Int = 0,
    val sessionsBlockingAppCount : Int = 0
)
