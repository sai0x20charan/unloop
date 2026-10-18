package com.charan.unloop.presentation.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.home.components.DurationInputField
import com.charan.unloop.presentation.home.components.InstantSessionSheet
import com.charan.unloop.presentation.home.components.PresetDurationSelector
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val selectedApps = remember(state.instantSessionUiModel.installedApps) {
        state.instantSessionUiModel.installedApps.filter { it.isSelected }
    }


    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomMediumTopBar(
                title = stringResource(R.string.navigation_home),
                scrollBehavior = scrollBehavior,
                showBackButton = false,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusManager.clearFocus()
                    if (state.instantSessionUiModel.isSessionTimeEditing) {
                        viewModel.onEvent(HomeEvent.OnEditSessionTime)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                TimerRing(
                    progress = if (state.startSessionState.isRunning && state.startSessionState.totalSeconds > 0) {
                        state.startSessionState.remainingSeconds / state.startSessionState.totalSeconds.toFloat()
                    } else {
                        1f
                    },
                    text = if (state.startSessionState.isRunning) {
                        state.startSessionState.remainingTimeFormatted
                    } else {
                        state.instantSessionUiModel.durationFormatted
                    },
                    onTimeChangeClick = {
                        viewModel.onEvent(HomeEvent.OnEditSessionTime)
                    },
                    hour = state.instantSessionUiModel.hour,
                    minute = state.instantSessionUiModel.minute,
                    isRunning = state.startSessionState.isRunning,
                    isEditing = state.instantSessionUiModel.isSessionTimeEditing,
                    onHourChange = {
                        viewModel.onEvent(HomeEvent.OnHourChange(it))
                    },
                    onMinuteChange = {
                        viewModel.onEvent(HomeEvent.OnMinuteChange(it))
                    },
                )

                AnimatedVisibility(visible = !state.startSessionState.isRunning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(24.dp))
                        PresetDurationSelector(
                            presets = state.instantSessionUiModel.durationPresets,
                            hour = state.instantSessionUiModel.hour,
                            minute = state.instantSessionUiModel.minute,
                            isHourValid = state.instantSessionUiModel.isHourValid,
                            isMinutesValid = state.instantSessionUiModel.isMinutesValid,
                            onPresetSelect = {
                                if (state.instantSessionUiModel.isSessionTimeEditing) {
                                    viewModel.onEvent(HomeEvent.OnEditSessionTime)
                                }
                                focusManager.clearFocus()
                                viewModel.onEvent(HomeEvent.OnPresetDurationSelect(it))
                            },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                SelectedBlockedAppsCard(
                    selectedApps = selectedApps,
                    onClick = {
                        if (state.instantSessionUiModel.isSessionTimeEditing) {
                            viewModel.onEvent(HomeEvent.OnEditSessionTime)
                        }
                        focusManager.clearFocus()
                        viewModel.onEvent(HomeEvent.OnInstantSessionCustomizeToggle)
                    },
                )

                Spacer(Modifier.height(32.dp))

                AnimatedContent(targetState = state.startSessionState.isRunning, label = "sessionAction") { running ->
                    if (running) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.onEvent(HomeEvent.OnEndSessionClick)
                            },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.home_end_session))
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.onEvent(HomeEvent.OnStartSessionClick)
                                focusManager.clearFocus()
                            },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.home_start_session))
                        }
                    }
                }
            }
        }
    }

    if (state.instantSessionUiModel.showBottomSheet) {
        InstantSessionSheet(
            apps = state.instantSessionUiModel.installedApps,
            onAppSelect = { viewModel.onEvent(HomeEvent.OnAppClicked(it)) },
            onAllAppsSelect = { viewModel.onEvent(HomeEvent.OnSelectAllApps) },
            onClose = { viewModel.onEvent(HomeEvent.OnInstantSessionCustomizeToggle) },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TimerRing(
    progress: Float,
    text: String,
    onTimeChangeClick: (() -> Unit)? = null,
    isRunning: Boolean = false,
    isEditing: Boolean = false,
    hour: Int = 0,
    minute: Int = 0,
    onHourChange: (String) -> Unit = {},
    onMinuteChange: (String) -> Unit = {},
) {
    val density = LocalDensity.current
    val stroke = with(density) { Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round) }
    val focusManager = LocalFocusManager.current
    val hourFocusRequester = remember { FocusRequester() }
    val minuteFocusRequester = remember { FocusRequester() }

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
            .size(250.dp)
            .clip(CircleShape)
            .clickable(!isRunning) { onTimeChangeClick?.invoke() },
    ) {
        CircularProgressIndicator(
            progress = { progress },
            strokeWidth = 10.dp,
            modifier = Modifier.size(250.dp),
        )

        AnimatedContent(
            targetState = isEditing && !isRunning,
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
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.home_hours),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            DurationInputField(
                                value = hour,
                                onValueChange = onHourChange,
                                isValid = true,
                                boxWidth = 76.dp,
                                focus = hourFocusRequester,
                                imeAction = ImeAction.Next,
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "",
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Box(
                                modifier = Modifier
                                    .height(64.dp)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = ":",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.home_minutes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            DurationInputField(
                                value = minute,
                                onValueChange = onMinuteChange,
                                isValid = true,
                                boxWidth = 76.dp,
                                imeAction = ImeAction.Done,
                                focus = minuteFocusRequester,
                                onDone = {
                                    focusManager.clearFocus()
                                    onTimeChangeClick?.invoke()
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    FilledTonalIconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onTimeChangeClick?.invoke()
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Done",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.displayMediumEmphasized,
                )
            }
        }
    }
}

@Preview(name = "Home", showBackground = true)
@Preview(name = "Home dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    UnloopTheme(dynamicColor = false) {
        HomeScreen()
    }
}
