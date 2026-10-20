package com.charan.unloop.data.model

import androidx.annotation.StringRes
import com.charan.unloop.R

enum class ThemeOptions {
    SYSTEM_DEFAULT,
    LIGHT,
    DARK;

    @StringRes
    fun toResId(): Int {
        return when (this) {
            SYSTEM_DEFAULT -> R.string.system
            LIGHT -> R.string.light
            DARK -> R.string.dark
        }
    }
}
