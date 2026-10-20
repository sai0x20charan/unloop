package com.charan.unloop.presentation.routine.pages

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
import com.charan.unloop.presentation.routine.components.RoutineLazyColumn
import com.charan.unloop.presentation.routine.components.NumberPicker
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.DateTimeUtils.toLocale
import com.swmansion.kmpwheelpicker.rememberWheelPickerState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import java.time.format.TextStyle
import com.charan.unloop.presentation.common.models.RoutineUiModel
import com.charan.unloop.presentation.routine.RoutineEvent

@Composable
fun ScheduleRoutinePage(
    routine: RoutineUiModel,
    showIntervalDialog: Boolean,
    onEvent: (RoutineEvent) -> Unit,
    modifier: Modifier = Modifier,
    dayInitials: List<DayOfWeek> = DateTimeUtils.getDaysOfWeek(),
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
) {

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
                    value = routine.name,
                    onValueChange = { onEvent(RoutineEvent.OnRoutineNameChange(it)) },
                    label = { Text("Enter routine name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                SectionLabel("Select Days")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween
                    )
                ) {
                    dayInitials.forEachIndexed { index, day ->
                        val isSelected = routine.selectedDays.contains(day)

                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = {
                                onEvent(RoutineEvent.OnRoutineDaySelect(day))
                            },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                dayInitials.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription =
                                        day.toLocale(TextStyle.FULL_STANDALONE) +
                                                if (isSelected) ", selected" else ", not selected"
                                },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = day.toLocale(TextStyle.NARROW_STANDALONE),
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeCard(
                                label = "Start Time",
                                value = routine.startTime?.toDisplayString() ?: "Select time",
                                icon = Icons.Rounded.MoreTime,
                                onClick = { onEvent(RoutineEvent.OnStartTimeToggle) },
                                modifier = Modifier.weight(1f)
                            )
                            TimeCard(
                                label = "End Time",
                                value = routine.endTime?.toDisplayString() ?: "Select time",
                                icon = Icons.Rounded.MoreTime,
                                onClick = { onEvent(RoutineEvent.OnEndTimeToggle) },
                                modifier = Modifier.weight(1f)
                            )
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
        color = MaterialTheme.colorScheme.secondary,
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
