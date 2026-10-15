package com.charan.unloop.presentation.routine.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swmansion.kmpwheelpicker.WheelPicker
import com.swmansion.kmpwheelpicker.WheelPickerState

@Composable
fun NumberPicker(
    state: WheelPickerState,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    formatter: (Int) -> String = { String.format("%02d", it) },
) {

    LaunchedEffect(state) {
        snapshotFlow { state.index }
            .collect { onValueChange(it) }
    }
    WheelPicker(
        state = state,
        bufferSize = 2,
        window = {
            Box(
                Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.shapes.small,
                ).padding(vertical = 4.dp)
            )
        },
    ) { index ->
        Text(
            text = formatter(index),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}