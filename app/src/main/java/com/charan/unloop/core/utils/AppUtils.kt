package com.charan.unloop.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import com.charan.unloop.BuildConfig
import com.charan.unloop.R
import com.charan.unloop.presentation.common.ToastMessage
import com.charan.unloop.presentation.common.showToast
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun getAppVersionWithVersionCode() =
    "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

@OptIn(ExperimentalUuidApi::class)
fun generateUuid(): String = Uuid.generateV4().toString()

fun Context.launchFeedbackEmail() {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject))
        putExtra(Intent.EXTRA_TEXT, generateFeedbackEmailBody())
    }
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        showToast(ToastMessage.Res(R.string.no_email_client))
    }
}

private fun Context.generateFeedbackEmailBody(): String = buildString {
    append("\n\n")
    appendLine("---")
    appendLine(getString(R.string.feedback_device_app_info))
    appendLine(getString(R.string.feedback_app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
    appendLine(getString(R.string.feedback_android_version, Build.VERSION.RELEASE, Build.VERSION.SDK_INT))
    appendLine(getString(R.string.feedback_device, Build.MANUFACTURER, Build.MODEL))
    appendLine("---")
}

fun Context.launchUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: ActivityNotFoundException) {
        showToast(ToastMessage.Res(R.string.no_browser_found))
    }
}
