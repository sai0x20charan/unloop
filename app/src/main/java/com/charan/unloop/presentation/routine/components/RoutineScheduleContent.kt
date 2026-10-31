package com.charan.unloop.presentation.routine.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ElevatedToggleButton
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.DateTimeUtils.toLocale
import com.charan.unloop.presentation.common.models.RoutineUiModel
import com.charan.unloop.presentation.routine.RoutineEvent
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.time.format.TextStyle

@Composable
fun RoutineScheduleContent(
    selectedDays : List<DayOfWeek>,
    onEvent: (RoutineEvent) -> Unit,
    startTime : LocalTime,
    endTime : LocalTime,
    modifier: Modifier = Modifier,
) {
    val days = remember { DateTimeUtils.getDaysOfWeek() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.routine_repeat_on),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            days.forEachIndexed { index, day ->
                FilledTonalToggleButton(
                    colors = FilledTonalToggleButtonDefaults
                        .colors(
                            containerColor = FilledTonalToggleButtonDefaults.colors().containerColor.copy(alpha = 0.5f),
                            checkedContainerColor = MaterialTheme.colorScheme.primary),
                    checked = day in selectedDays,
                    onCheckedChange = { onEvent(RoutineEvent.OnRoutineDaySelect(day)) },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        days.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = day.toLocale(TextStyle.FULL_STANDALONE) },
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = day.toLocale(TextStyle.NARROW_STANDALONE),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoutineTimeField(
                label = stringResource(R.string.routine_start_time),
                value = startTime.toDisplayString(),
                onClick = { onEvent(RoutineEvent.OnStartTimeToggle) },
                modifier = Modifier.weight(1f),
            )
            RoutineTimeField(
                label = stringResource(R.string.routine_end_time),
                value = endTime.toDisplayString(),
                onClick = { onEvent(RoutineEvent.OnEndTimeToggle) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RoutineTimeField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
