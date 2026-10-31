package com.charan.unloop.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.charan.unloop.R
import kotlin.math.roundToInt

@Composable
fun WaitTimeSlider(
    seconds: Int,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.routine_wait_time)
    val sliderState = rememberSliderState(
        value = seconds.toFloat(),
        steps = 22,
        trackRange = 5f..120f,
    )
    sliderState.value = seconds.toFloat()

    Column(modifier = modifier) {
        Text(
            text = pluralStringResource(R.plurals.routine_wait_seconds, seconds, seconds),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Slider(
            state = sliderState,
            onValueChange = { onSecondsChange(it.roundToInt()) },
            modifier = Modifier.semantics { contentDescription = label },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = pluralStringResource(R.plurals.routine_wait_seconds, 5, 5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pluralStringResource(R.plurals.routine_wait_seconds, 120, 120),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
