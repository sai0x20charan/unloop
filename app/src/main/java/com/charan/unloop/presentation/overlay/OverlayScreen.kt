package com.charan.unloop.presentation.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charan.unloop.core.utils.DateUtils.toDisplayString
import com.charan.unloop.presentation.routine.UnlockChallengeType
import com.charan.unloop.ui.theme.UnloopTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OverLayScreen(
    unlockType: UnlockChallengeType,
    routineName: String,
    appName: String,
    endTime : LocalTime,
    waitTimeSeconds: Int,
    onKeepFocusing: () -> Unit = {},
    onUnlock: () -> Unit = {}
) {
    val strict = unlockType == UnlockChallengeType.STRICT_MODE
    val interactionSource = remember(unlockType, routineName, appName, waitTimeSeconds) {
        MutableInteractionSource()
    }
    val isHolding by interactionSource.collectIsPressedAsState()
    var progress by remember(interactionSource) { mutableFloatStateOf(0f) }
    val currentOnUnlock by rememberUpdatedState(onUnlock)

    LaunchedEffect(isHolding, interactionSource) {
        progress = 0f
        if (isHolding && !strict) {

            val duration = waitTimeSeconds.coerceAtLeast(1) * 1_000_000_000L
            val startTime = withFrameNanos { it }

            while (isHolding && progress < 1f) {
                val currentTime = withFrameNanos { it }
                progress = ((currentTime - startTime).toDouble() / duration).toFloat()
                    .coerceIn(0f, 1f)
            }

            if (progress >= 1f) {
                currentOnUnlock()
            }
        }
    }
    UnloopTheme {
        val colors = MaterialTheme.colorScheme

        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            color = colors.surface) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(152.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = colors.primary,
                            trackColor = colors.primary.copy(alpha = 0.12f),
                            strokeWidth = 3.dp,
                            strokeCap = StrokeCap.Round
                        )

                        Box(
                            modifier = Modifier
                                .size(126.dp)
                                .border(
                                    3.dp,
                                    colors.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colors.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(30.dp)
                                        .size(40.dp),
                                    tint = colors.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = "$appName is ${if (strict) "locked" else "paused"}",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "This app is blocked while your routine is active.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))
                    RoutineDetailCard(
                        routineName = routineName,
                        remainingTime = if (strict) "Until routine ends" else endTime.toDisplayString(),
                        modifier = Modifier.fillMaxWidth(),
                        timeLabel = if (strict) "Blocked" else "Unlock delay"
                    )
                    Spacer(Modifier.height(28.dp))

                    if (!strict) {
                        Button(
                            onClick = {},
                            interactionSource = interactionSource
                        ) {
                            Text("Hold for $waitTimeSeconds seconds to unlock")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineDetailCard(
    routineName: String,
    remainingTime: String,
    modifier: Modifier = Modifier,
    timeLabel: String = "Unlock delay"
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Active routine",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = routineName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 18.dp),
                color = colors.outlineVariant.copy(alpha = 0.6f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.HourglassEmpty,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.onSurfaceVariant
                )
                Text(
                    text = timeLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                Text(
                    text = remainingTime,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }

        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun OverlayScreenPreview() {
    OverLayScreen(
        unlockType = UnlockChallengeType.WAIT_TIME,
        routineName = "Morning Routine",
        appName = "YouTube",
        endTime = LocalTime(10, 30),
        waitTimeSeconds = 5
    )
}
