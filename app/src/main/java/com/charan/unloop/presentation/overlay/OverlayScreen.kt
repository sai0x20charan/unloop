package com.charan.unloop.presentation.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OverLayScreen(){
    Scaffold() { innerPadding->
        Column(
            modifier = Modifier.padding(innerPadding).background(MaterialTheme.colorScheme.primary),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally

        ) {
            Text("You are in Overlay Screen", color = MaterialTheme.colorScheme.onPrimary)

        }

    }
}