package com.charan.stepaway.presentation.routine.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import com.charan.stepaway.presentation.routine.components.RoutineLazyColumn
import com.charan.stepaway.presentation.routine.components.NumberPicker
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.charan.stepaway.core.utils.DateUtils
import com.charan.stepaway.core.utils.DateUtils.toLocale
import com.swmansion.kmpwheelpicker.rememberWheelPickerState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import java.time.format.TextStyle
import com.charan.stepaway.presentation.routine.RoutineScheduleState
import com.charan.stepaway.presentation.routine.RoutineEvent

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleRoutinePage(
    routineName: String,
    scheduleState: RoutineScheduleState,
    onEvent: (RoutineEvent) -> Unit,
    modifier: Modifier = Modifier,
    dayInitials: List<DayOfWeek> = DateUtils.getDaysOfWeek(),
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
) {
    var showIntervalDialog by remember { mutableStateOf(false) }

    RoutineLazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionLabel("Routine Name")
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { onEvent(RoutineEvent.OnRoutineNameChange(it)) },
                    label = { Text("Enter routine name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                SectionLabel("Select Days")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween,
                        Alignment.CenterHorizontally
                    )
                ) {
                    dayInitials.forEachIndexed { index, day ->
                        val isSelected = scheduleState.routineDays.contains(day)
                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = { onEvent(RoutineEvent.OnRoutineDaySelect(day)) },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                dayInitials.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            modifier = Modifier.semantics {
                                contentDescription = day.toLocale(TextStyle.FULL_STANDALONE) +
                                        if (isSelected) ", selected" else ", not selected"
                            }
                        ) {
                            Text(
                                day.toLocale(TextStyle.NARROW_STANDALONE),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("All Day", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = scheduleState.isAllDay,
                            onCheckedChange = { onEvent(RoutineEvent.OnAllDayToggle) }
                        )
                    }

                    AnimatedVisibility(
                        visible = !scheduleState.isAllDay,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeCard(
                                label = "Start Time",
                                value = scheduleState.routineStartTime.toString(),
                                icon = Icons.Rounded.MoreTime,
                                onClick = { onEvent(RoutineEvent.OnStartTimeClick) },
                                modifier = Modifier.weight(1f)
                            )
                            TimeCard(
                                label = "End Time",
                                value = scheduleState.routineEndTime.toString(),
                                icon = Icons.Rounded.MoreTime,
                                onClick = { onEvent(RoutineEvent.OnEndTimeClick) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Blocking Mode")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Interval Blocking", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Lock the phone periodically, not the whole window",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = scheduleState.isIntervalBlocking,
                            onCheckedChange = { onEvent(RoutineEvent.OnIntervalBlockingToggle) }
                        )
                    }

                    AnimatedVisibility(visible = scheduleState.isIntervalBlocking) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val intervalText = buildString {
                                if (scheduleState.intervalHours > 0) append("${scheduleState.intervalHours}h ")
                                append("${scheduleState.intervalMinutes}m")
                            }

                            TimeCard(
                                label = "Block Interval",
                                value = intervalText,
                                icon = Icons.Rounded.Timer,
                                onClick = { showIntervalDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Summary
                            val intervalStr = buildString {
                                if (scheduleState.intervalHours > 0) {
                                    append("${scheduleState.intervalHours} hr")
                                }
                                if (scheduleState.intervalMinutes > 0) {
                                    if (isNotEmpty()) append(" ")
                                    append("${scheduleState.intervalMinutes} min")
                                }
                                if (isEmpty()) {
                                    append("0 min")
                                }
                            }
                            val summaryText = "Every $intervalStr, Step Away locks your phone."

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Box(
                                    modifier = Modifier.padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = summaryText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showIntervalDialog) {
        IntervalPickerDialog(
            title = "Select Block Interval",
            initialHours = scheduleState.intervalHours,
            initialMinutes = scheduleState.intervalMinutes,
            onConfirm = { hours, minutes ->
                onEvent(RoutineEvent.OnIntervalHoursChange(hours))
                onEvent(RoutineEvent.OnIntervalMinutesChange(minutes))
            },
            onDismiss = { showIntervalDialog = false }
        )
    }
}

/**
 * Reusable dialog for choosing hours and minutes.
 */
@Composable
private fun IntervalPickerDialog(
    title: String,
    initialHours: Int,
    initialMinutes: Int,
    onConfirm: (hours: Int, minutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val hoursState = rememberWheelPickerState(
        itemCount = 24,
        initialIndex = initialHours
    )
    val minutesState = rememberWheelPickerState(
        itemCount = 60,
        initialIndex = initialMinutes
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HOURS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        NumberPicker(
                            state = hoursState,
                            onValueChange = {},
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MINUTES",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        NumberPicker(
                            state = minutesState,
                            onValueChange = {},
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onConfirm(hoursState.index, minutesState.index)
                        onDismiss()
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
private fun TimeCard(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = "$label, $value"
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
