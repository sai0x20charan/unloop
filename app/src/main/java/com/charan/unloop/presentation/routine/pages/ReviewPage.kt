package com.charan.unloop.presentation.routine.pages

import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.charan.unloop.presentation.routine.components.RoutineLazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.charan.unloop.core.utils.DateUtils.toDisplayString
import com.charan.unloop.presentation.routine.RoutineScheduleState
import com.charan.unloop.presentation.routine.UnlockChallengeType
import com.charan.unloop.presentation.routine.InstalledAppsUiModel

@Composable
fun ReviewPage(
    routineName: String,
    scheduleState: RoutineScheduleState,
    unlockChallengeType: UnlockChallengeType,
    waitTimeSeconds: Int,
    selectedApps: List<InstalledAppsUiModel>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
) {
    RoutineLazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Routine Name Section
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ROUTINE NAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = routineName.ifEmpty { "My Unloop Routine" },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider()

                // Schedule Details Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Schedule Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ACTIVE DAYS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val daysText = if (scheduleState.routineDays.isEmpty()) {
                                "Every day"
                            } else {
                                scheduleState.routineDays.joinToString(", ") { it.name.lowercase().replaceFirstChar { char -> char.titlecase() } }
                            }
                            Text(daysText, style = MaterialTheme.typography.bodyLarge)
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "TIME WINDOW",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val timeText = if (scheduleState.isAllDay) "All Day" else "${scheduleState.routineStartTime.toDisplayString()} - ${scheduleState.routineEndTime.toDisplayString()}"
                            Text(timeText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "BLOCKING FREQUENCY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (scheduleState.isIntervalBlocking) {
                            val intervalText = buildString {
                                if (scheduleState.intervalHours > 0) append("${scheduleState.intervalHours}h ")
                                append("${scheduleState.intervalMinutes}m")
                            }

                            Text("Locks every $intervalText", style = MaterialTheme.typography.bodyLarge)
                        } else {
                            val hrs = scheduleState.routineFrequency / 60
                            val mins = scheduleState.routineFrequency % 60
                            val repeatText = when {
                                hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
                                hrs > 0 -> "${hrs}h"
                                else -> "${mins}m"
                            }
                            Text("Repeats every $repeatText", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                HorizontalDivider()

                // Unlock Challenge Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Unlock Challenge Type",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val challengeText = when (unlockChallengeType) {
                        UnlockChallengeType.WAIT_TIME -> "Wait Time Challenge (${waitTimeSeconds} seconds delay)"
                        UnlockChallengeType.ONE_TIME_UNLOCK -> {
                            "Emergency One-time Unlock ($waitTimeSeconds seconds unlock delay)"
                        }
                        UnlockChallengeType.STRICT_MODE -> "Strict Mode (No unlocks allowed)"
                    }
                    Text(challengeText, style = MaterialTheme.typography.bodyLarge)
                }

                HorizontalDivider()

                // Blocked Apps Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Blocked Apps (${selectedApps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (selectedApps.isEmpty()) {
                        Text(
                            text = "No apps selected (routine will block nothing)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedApps.forEach { app ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        app.icon?.let { icon ->
                                            Image(
                                                bitmap = icon.toBitmap().asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = app.appName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
