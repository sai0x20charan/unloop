package com.charan.unloop.presentation.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.presentation.routine.UnlockChallengeType
import com.charan.unloop.presentation.theme.UnloopTheme
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OverLayScreen(
    unlockType: UnlockChallengeType,
    routineName: String,
    appName: String,
    endTime: String,
    waitTimeSeconds: Int,
    onKeepFocusing: () -> Unit = {},
    onUnlock: () -> Unit = {}
) {
    val strict = unlockType == UnlockChallengeType.STRICT_MODE
    val interactionSource = remember(unlockType, routineName, appName, waitTimeSeconds) {
        MutableInteractionSource()
    }
    val isHolding by interactionSource.collectIsPressedAsState()
    val progress = remember { Animatable(0f) }
    val currentOnUnlock by rememberUpdatedState(onUnlock)


    LaunchedEffect(isHolding, strict) {
        progress.snapTo(0f)
        if (isHolding && !strict) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = waitTimeSeconds.coerceAtLeast(1) * 1000,
                    easing = LinearEasing
                )
            )
            currentOnUnlock()
        }
    }
    val shownProgress by animateFloatAsState(
        targetValue = progress.value,
        animationSpec = if (isHolding) snap() else spring(),
        label = "unlockProgress"
    )

    UnloopTheme {
        val colors = MaterialTheme.colorScheme

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colors.primaryContainer.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height * 0.3f),
                                radius = size.width * 0.9f
                            ),
                            radius = size.width * 0.9f,
                            center = Offset(size.width / 2f, size.height * 0.3f)
                        )
                    }
                    .safeDrawingPadding()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OverlayHero(
                        strict = strict,
                        isHolding = isHolding,
                    )
                    Spacer(Modifier.height(32.dp))
                    RoutineStatus(
                        routineName = routineName,
                        endTime = endTime
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(
                            if (strict) R.string.overlay_app_locked else R.string.overlay_app_paused,
                            appName
                        ),
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        color = colors.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.overlay_app_blocked_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onKeepFocusing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ButtonDefaults.MediumContainerHeight)
                    ) {
                        Text(
                            text = stringResource(R.string.overlay_keep_focusing),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (!strict) {
                        HoldToUnlockButton(
                            label = pluralStringResource(
                                R.plurals.overlay_hold_to_unlock,
                                waitTimeSeconds,
                                waitTimeSeconds
                            ),
                            progress = { shownProgress },
                            interactionSource = interactionSource,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OverlayHero(
    strict: Boolean,
    isHolding: Boolean, ) {
    val colors = MaterialTheme.colorScheme
    val rotation by rememberInfiniteTransition(label = "heroRotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heroRotationValue"
    )

    Box(
        modifier = Modifier.size(232.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(176.dp)
                .rotate(rotation)
                .background(
                    color =  colors.primaryContainer,
                    shape = MaterialShapes.Cookie12Sided.toShape()
                )
        )
        AnimatedContent(
            targetState = isHolding,
            transitionSpec = {
                (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith
                    (scaleOut(targetScale = 0.6f) + fadeOut())
            },
            label = "heroIcon"
        ) { holding ->
            Icon(
                imageVector = if (holding) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint =  colors.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RoutineStatus(
    routineName: String,
    endTime: String
) {
    val colors = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(colors.primary, CircleShape)
        )
        Text(
            text = routineName,
            style = MaterialTheme.typography.titleSmall,
            color = colors.primary
        )
        Text(
            text = "\u00B7",
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.overlay_until_time, endTime),
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun HoldToUnlockButton(
    label: String,
    progress: () -> Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(ButtonDefaults.MediumContainerHeight)
            .clip(CircleShape)
            .background(colors.surfaceContainerHighest)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(progress().coerceIn(0f, 1f))
                .background(colors.primaryContainer)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun OverlayScreenPreview() {
    OverLayScreen(
        unlockType = UnlockChallengeType.WAIT_TIME,
        routineName = stringResource(R.string.routine_example_morning),
        appName = stringResource(R.string.home_app_youtube),
        endTime = LocalTime(10, 30).toDisplayString(),
        waitTimeSeconds = 5
    )
}

@Preview(showSystemUi = true)
@Composable
fun OverlayScreenStrictPreview() {
    OverLayScreen(
        unlockType = UnlockChallengeType.STRICT_MODE,
        routineName = stringResource(R.string.routine_example_morning),
        appName = stringResource(R.string.home_app_youtube),
        endTime = LocalTime(10, 30).toDisplayString(),
        waitTimeSeconds = 5
    )
}
