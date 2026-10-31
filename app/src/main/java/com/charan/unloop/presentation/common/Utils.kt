package com.charan.unloop.presentation.common

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

sealed class ToastMessage {
    data class Text(val text: String) : ToastMessage()
    data class Res(@param:StringRes val resId: Int) : ToastMessage()
}

fun Context.showToast(toastMessage: ToastMessage) {
    when (toastMessage) {
        is ToastMessage.Res -> {
            Toast.makeText(
                this,
                toastMessage.resId,
                Toast.LENGTH_SHORT
            ).show()
        }
        is ToastMessage.Text -> {
            Toast.makeText(
                this,
                toastMessage.text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
