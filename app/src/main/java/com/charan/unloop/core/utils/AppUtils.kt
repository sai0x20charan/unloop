package com.charan.unloop.core.utils

import com.charan.unloop.BuildConfig

fun getAppVersionWithVersionCode() =
    "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"