package com.charan.stepaway.presentation.routine.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.charan.stepaway.presentation.routine.RoutineEvent
import com.charan.stepaway.presentation.routine.UnlockChallengeType
import com.charan.stepaway.presentation.routine.components.NumberPicker
import com.charan.stepaway.presentation.routine.components.RoutineLazyColumn
import com.swmansion.kmpwheelpicker.rememberWheelPickerState

@Composable
fun UnlockChallengePage(
    unlockChallengeType: UnlockChallengeType,
    waitTimeSeconds: Int,
    onEvent: (RoutineEvent) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
) {

    RoutineLazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        item {
            Text(
                text = "Choose how you can temporarily unlock blocked apps if needed. Adding barrier types helps prevent impulsive use.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChallengeOptionCard(
                    title = "Wait Time Challenge",
                    description = "Requires you to wait for a set duration before the app opens. Helps break instant gratification loops.",
                    icon = Icons.Rounded.HourglassEmpty,
                    isSelected = unlockChallengeType == UnlockChallengeType.WAIT_TIME,
                    onClick = { onEvent(RoutineEvent.OnUnlockChallengeTypeChange(UnlockChallengeType.WAIT_TIME)) }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Wait Duration",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "$waitTimeSeconds seconds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = waitTimeSeconds.toFloat(),
                            onValueChange = { onEvent(RoutineEvent.OnWaitTimeSecondsChange(it.toInt())) },
                            valueRange = 5f..120f,
                            steps = 22
                        )
                    }
                }

                ChallengeOptionCard(
                    title = "Emergency One-time Unlock",
                    description = "Allows a single unlock per app per session. Forces a custom delay time before opening.",
                    icon = Icons.Rounded.LockOpen,
                    isSelected = unlockChallengeType == UnlockChallengeType.ONE_TIME_UNLOCK,
                    onClick = { onEvent(RoutineEvent.OnUnlockChallengeTypeChange(UnlockChallengeType.ONE_TIME_UNLOCK)) }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Wait Duration",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "$waitTimeSeconds seconds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = waitTimeSeconds.toFloat(),
                            onValueChange = { onEvent(RoutineEvent.OnWaitTimeSecondsChange(it.toInt())) },
                            valueRange = 5f..120f,
                            steps = 22
                        )
                    }
                }

                ChallengeOptionCard(
                    title = "Strict Mode",
                    description = "No unlocks allowed under any circumstance. Pure blocking logic.",
                    icon = Icons.Rounded.Lock,
                    isSelected = unlockChallengeType == UnlockChallengeType.STRICT_MODE,
                    onClick = { onEvent(RoutineEvent.OnUnlockChallengeTypeChange(UnlockChallengeType.STRICT_MODE)) }
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Caution: Permanent Lockout",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "You will not be able to bypass or unlock any blocked apps during the scheduled period.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (content != null) {
                AnimatedVisibility(visible = isSelected) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        content()
                    }
                }
            }
        }
    }
}