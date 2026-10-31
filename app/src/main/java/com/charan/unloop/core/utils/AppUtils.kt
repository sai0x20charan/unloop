package com.charan.unloop.core.utils

import android.content.Context
import com.charan.unloop.BuildConfig
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun getAppVersionWithVersionCode() =
    "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

@OptIn(ExperimentalUuidApi::class)
fun generateUuid(): String = Uuid.generateV4().toString()


