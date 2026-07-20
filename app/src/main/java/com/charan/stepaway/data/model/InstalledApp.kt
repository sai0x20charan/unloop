package com.charan.stepaway.data.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.vector.ImageVector

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable
)