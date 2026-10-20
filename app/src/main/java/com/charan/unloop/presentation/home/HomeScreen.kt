package com.charan.unloop.presentation.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.home.components.DurationInputField
import com.charan.unloop.presentation.home.components.PresetDurationSelector
import com.charan.unloop.presentation.home.components.SelectAppsSheet
import com.charan.unloop.presentation.home.components.SelectedBlockedAppsCard
import com.charan.unloop.ui.theme.UnloopTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val isSessionRunning = state.startSessionState.isRunning
    val isSessionPaused = state.startSessionState.isPaused
    val isSessionActive = isSessionRunning || isSessionPaused
    val mediumButtonSize = ButtonDefaults.MediumContainerHeight
    val mediumButtonIconSize = ButtonDefaults.iconSizeFor(mediumButtonSize)

    Scaffold(modifier = modifier) { innerPadding ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            contentPadding = innerPadding.toScreenContentPadding()
        ) {
            item {

                TimerRing(
                    progress = if (isSessionActive && state.startSessionState.totalSeconds > 0) {
                        state.startSessionState.progress
                    } else {
                        1f
                    },
                    text = if (isSessionActive) state.startSessionState.remainingTimeFormatted else state.instantSessionUiModel.durationFormatted,
                    endTimeText = state.startSessionState.endTimeFormatted,
                    onTimeChangeClick = {
                        focusManager.clearFocus()
                        viewModel.onEvent(HomeEvent.OnEditSessionTime)
                    },
                    hour = state.instantSessionUiModel.hour,
                    minute = state.instantSessionUiModel.minute,
                    isSessionActive = isSessionActive,
                    isPaused = isSessionPaused,
                    isEditing = state.instantSessionUiModel.isSessionTimeEditing,
                    onHourChange = { viewModel.onEvent(HomeEvent.OnHourChange(it)) },
                    onMinuteChange = { viewModel.onEvent(HomeEvent.OnMinuteChange(it)) },
                )

                Spacer(Modifier.height(20.dp))

                AnimatedVisibility(visible = !isSessionActive) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        PresetDurationSelector(
                            presets = state.instantSessionUiModel.durationPresets,
                            hour = state.instantSessionUiModel.hour,
                            minute = state.instantSessionUiModel.minute,
                            isHourValid = state.instantSessionUiModel.isHourValid,
                            isMinutesValid = state.instantSessionUiModel.isMinutesValid,
                            onPresetSelect = {
                                viewModel.onEvent(HomeEvent.OnPresetDurationSelect(it))
                            },
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                AnimatedVisibility(visible = !isSessionActive) {

                    SelectedBlockedAppsCard(
                        selectedApps = state.instantSessionUiModel.installedApps.filter { it.isSelected },
                        enabled = true,
                        title = stringResource(
                            R.string.home_apps_to_block
                        ),
                        onClick = {
                            viewModel.onEvent(HomeEvent.OnInstantSessionCustomizeToggle)
                        },
                    )
                }


                Spacer(Modifier.height(32.dp))

                AnimatedContent(
                    targetState = isSessionActive,
                    label = "sessionAction",
                    modifier = Modifier.fillMaxWidth(),
                ) { sessionActive ->
                    if (sessionActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = {
                                    viewModel.onEvent(HomeEvent.OnPauseSessionClick)
                                },
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(mediumButtonSize),
                            ) {
                                AnimatedContent(
                                    targetState = isSessionPaused,
                                    label = "pauseResumeButtonContent",
                                ) { paused ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = if (paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                            contentDescription = null,
                                            modifier = Modifier.size(mediumButtonIconSize),
                                        )
                                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                        Text(
                                            text = stringResource(
                                                if (paused) R.string.home_resume_session else R.string.home_pause_session
                                            ),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }
                                }
                            }

                            FilledTonalButton(
                                onClick = { viewModel.onEvent(HomeEvent.OnEndSessionClick) },
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(mediumButtonSize),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(mediumButtonIconSize),
                                )
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text(
                                    text = stringResource(R.string.home_end_session),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    viewModel.onEvent(HomeEvent.OnStartSessionClick)
                                },
                                enabled = state.instantSessionUiModel.canStart,
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(mediumButtonSize),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(mediumButtonIconSize),
                                )
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text(
                                    text = stringResource(R.string.home_start_session),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    if (state.instantSessionUiModel.showBottomSheet) {
        SelectAppsSheet(
            apps = state.instantSessionUiModel.installedApps,
            onAppSelect = { viewModel.onEvent(HomeEvent.OnAppClicked(it)) },
            onAllAppsSelect = { viewModel.onEvent(HomeEvent.OnSelectAllApps) },
            onClose = { viewModel.onEvent(HomeEvent.OnAppsSheetDismiss) },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TimerRing(
    progress: Float,
    text: String,
    endTimeText: String,
    onTimeChangeClick: () -> Unit,
    isSessionActive: Boolean,
    isPaused: Boolean,
    isEditing: Boolean,
    hour: Int,
    minute: Int,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val hourFocusRequester = remember { FocusRequester() }
    val minuteFocusRequester = remember { FocusRequester() }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
        label = "timerProgress"
    )

    LaunchedEffect(isEditing) {
        if (isEditing) {
            delay(120.milliseconds)
            try {
                hourFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(280.dp)
            .clip(CircleShape)
            .clickable(enabled = !isSessionActive && !isEditing, onClick = onTimeChangeClick),
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            strokeWidth = 10.dp,
            color = if (isPaused) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            },
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.size(280.dp),
        )

        AnimatedContent(
            targetState = isEditing && !isSessionActive,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 40, easing = LinearOutSlowInEasing)) +
                    scaleIn(initialScale = 0.9f, animationSpec = tween(220, delayMillis = 40, easing = LinearOutSlowInEasing)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(160)) +
                            scaleOut(targetScale = 0.9f, animationSpec = tween(160))
                    )
            },
            contentAlignment = Alignment.Center,
            label = "timerRingEditingTransition",
        ) { editing ->
            if (editing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.home_set_duration),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DurationColumn(label = stringResource(R.string.home_hours)) {
                            DurationInputField(
                                value = hour,
                                onValueChange = onHourChange,
                                isValid = true,
                                boxWidth = 76.dp,
                                focus = hourFocusRequester,
                                imeAction = ImeAction.Next,
                            )
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 20.dp),
                        )

                        DurationColumn(label = stringResource(R.string.home_minutes)) {
                            DurationInputField(
                                value = minute,
                                onValueChange = onMinuteChange,
                                isValid = true,
                                boxWidth = 76.dp,
                                imeAction = ImeAction.Done,
                                focus = minuteFocusRequester,
                                onDone = onTimeChangeClick,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onTimeChangeClick()
                        },
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = ButtonDefaults.SmallContentPadding,
                        modifier = Modifier,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier,
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.home_done))
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isSessionActive) {
                        if (isPaused) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(bottom = 6.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Pause,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = stringResource(R.string.home_session_paused),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.home_remaining),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.displayLargeEmphasized,
                        color = if (isPaused) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Spacer(Modifier.height(8.dp))
                    if (isSessionActive) {
                        if (endTimeText.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.home_ends_at, endTimeText),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(R.string.home_tap_to_edit),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
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
private fun DurationColumn(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}
