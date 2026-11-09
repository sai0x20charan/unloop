package com.charan.unloop.data.model

import androidx.annotation.StringRes
import com.charan.unloop.R

enum class TimeFormatOptions {
    SYSTEM_DEFAULT,
    TWELVE_HOUR,
    TWENTY_FOUR_HOUR;

    @StringRes
    fun toResId(): Int = when (this) {
        SYSTEM_DEFAULT -> R.string.system
        TWELVE_HOUR -> R.string.time_format_12_hour
        TWENTY_FOUR_HOUR -> R.string.time_format_24_hour
    }
}
