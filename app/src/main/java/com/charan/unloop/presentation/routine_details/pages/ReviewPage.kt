package com.charan.unloop.presentation.routine_details.pages

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
import com.charan.unloop.presentation.routine_details.components.RoutineLazyColumn
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.charan.unloop.R
import com.charan.unloop.presentation.common.mapper.toFormattedDays
import com.charan.unloop.presentation.common.mapper.toFormattedTime
import com.charan.unloop.presentation.common.model.AppInfoUiModel
import com.charan.unloop.presentation.common.model.RoutineUiModel
import com.charan.unloop.data.local.model.enums.UnlockChallengeType

@Composable
fun ReviewPage(
    routine: RoutineUiModel,
    selectedApps: List<AppInfoUiModel>,
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
                        text = stringResource(R.string.routine_name),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = routine.name.ifEmpty { stringResource(R.string.routine_default_name) },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider()

                // Schedule Details Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.routine_schedule_details),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.routine_active_days),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val daysText = routine.toFormattedDays()
                            Text(daysText, style = MaterialTheme.typography.bodyLarge)
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.routine_time_window),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val timeText = routine.toFormattedTime()
                            Text(timeText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.routine_blocking_frequency),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                            Text(stringResource(R.string.routine_blocking_continuous), style = MaterialTheme.typography.bodyLarge)

                    }
                }

                HorizontalDivider()

                // Unlock Challenge Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.routine_focus_rules),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val challengeText = when (routine.unlockChallengeType) {
                        UnlockChallengeType.WAIT_TIME -> stringResource(
                            R.string.routine_list_metadata,
                            stringResource(R.string.routine_wait_to_unlock),
                            pluralStringResource(
                                R.plurals.routine_wait_seconds,
                                routine.waitDurationSeconds ?: 30,
                                routine.waitDurationSeconds ?: 30,
                            ),
                        )

                        UnlockChallengeType.STRICT_MODE -> stringResource(R.string.routine_no_unlocks)
                    }
                    Text(challengeText, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = stringResource(
                            if (routine.areSessionControlsLocked) R.string.session_controls_locked
                            else R.string.routine_session_controls_available
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                HorizontalDivider()

                // Blocked Apps Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.routine_blocked_apps_count, selectedApps.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (selectedApps.isEmpty()) {
                        Text(
                            text = stringResource(R.string.routine_no_apps_selected),
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
