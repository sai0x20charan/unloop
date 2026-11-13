package com.charan.unloop.presentation.settings.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LoadingDialog(
    onDismiss : () -> Unit= {},
    title : String
) {

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(title)
        },
        text = {
            LinearWavyProgressIndicator()
        },
        confirmButton = {},
        dismissButton = {}
    )
}