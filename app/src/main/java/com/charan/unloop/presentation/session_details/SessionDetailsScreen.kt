package com.charan.unloop.presentation.session_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.DateTimeUtils.toFocusDuration
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.presentation.common.components.BlockedAppListItem
import com.charan.unloop.presentation.common.components.CustomTopBar
import com.charan.unloop.presentation.common.components.SectionHeader
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.common.model.FocusSegmentUiModel
import com.charan.unloop.presentation.theme.indexItemFor
import kotlin.math.roundToInt

@Composable
fun SessionDetailsScreen(
    sessionId: String,
    onBack: () -> Unit,
) {
    val viewModel: SessionDetailsViewModel = hiltViewModel<SessionDetailsViewModel, SessionDetailsViewModel.Factory>(
        creationCallback = { factory -> factory.create(sessionId) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val resource = LocalResources.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SessionDetailsEffect.NavigateBack -> onBack()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTopBar(
                title = stringResource(R.string.session_details_title),
                onBackClick = { viewModel.onEvent(SessionDetailsEvent.OnBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding.toScreenContentPadding()
        ) {
            item {
                Text(
                    text = state.sessionName,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(
                        R.string.session_details_date_and_type,
                        state.createdAt.date.toDisplayString(),
                        stringResource(
                            when (state.sessionType) {
                                SessionType.INSTANT -> R.string.home_instant_session
                                SessionType.SCHEDULED -> R.string.session_details_scheduled_session
                            }
                        )
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Spacer(Modifier.height(28.dp))

                Text(
                    text = state.totalFocusDurationSeconds.toFocusDuration(resource),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.session_details_focus_time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FocusDurationBar(
                    focusSegments = state.focusSegments,
                    stoppedTime = state.actualEndTime?.toDisplayString(state.is24Hour),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.createdAt.toDisplayString(state.is24Hour),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (state.endTime.date == state.createdAt.date) {
                            state.endTime.toDisplayString(state.is24Hour)
                        } else {
                            stringResource(
                                R.string.sessions_date_and_time,
                                state.endTime.date.toDisplayString(),
                                state.endTime.toDisplayString(state.is24Hour)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = stringResource(
                                R.string.session_details_focused_duration,
                                state.totalFocusDurationSeconds.toFocusDuration(resource)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                        )
                        Text(
                            text = stringResource(
                                R.string.session_details_paused_duration,
                                state.totalPausedDurationSeconds.toFocusDuration(resource)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

            }

            item {
                SectionHeader(stringResource(R.string.session_details_app_activity))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = state.blockedApps.size.toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = stringResource(R.string.home_blocked_apps),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = state.appAccessGrantedCount.toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = stringResource(R.string.session_details_app_access_granted),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            }

            item {
                SectionHeader(
                    title = stringResource(R.string.home_blocked_apps)
                )
            }
            itemsIndexed(items = state.blockedApps) { index, item ->
                BlockedAppListItem(
                    appName = item.appName,
                    appIcon = item.appIcon,
                    blockedCount = item.blockedCount,
                    accessGrantedCount = item.accessGrantedCount,
                    indexItem = state.blockedApps.indexItemFor(index)
                )
            }
        }
    }
}

@Composable
fun FocusDurationBar(
    focusSegments: List<FocusSegmentUiModel>,
    modifier: Modifier = Modifier,
    stoppedTime: String? = null,
) {
    val focusColor = MaterialTheme.colorScheme.primary
    val pauseColor = MaterialTheme.colorScheme.tertiaryContainer
    val endedColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val stoppedFraction = focusSegments.firstOrNull { it.isEnded }?.startFraction
    val showStoppedMarker = stoppedFraction != null && stoppedTime != null

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val barWidth = maxWidth
        Column {
            if (showStoppedMarker) {
                var labelWidth by remember { mutableIntStateOf(0) }
                Text(
                    text = stringResource(R.string.session_details_stopped_at, stoppedTime),
                    style = MaterialTheme.typography.labelSmall,
                    onTextLayout = { labelWidth = it.size.width },
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .offset {
                            val markerX = (barWidth.toPx() * stoppedFraction).roundToInt()
                            IntOffset(
                                x = (markerX - labelWidth)
                                    .coerceIn(0, (barWidth.roundToPx() - labelWidth).coerceAtLeast(0)),
                                y = 0,
                            )
                        }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (showStoppedMarker) 20.dp else 12.dp)
            ) {
                focusSegments.forEach { (startFraction, endFraction, isFocused, isEnded) ->
                    val segmentColor = when {
                        isEnded -> endedColor
                        isFocused -> focusColor
                        else -> pauseColor
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = barWidth * startFraction)
                            .fillMaxWidth(endFraction - startFraction)
                            .clip(RoundedCornerShape(50))
                            .height(12.dp)
                            .background(segmentColor)
                    )
                }
                if (showStoppedMarker) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = barWidth * stoppedFraction)
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
    }
}
