package com.charan.unloop.presentation.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.DateTimeUtils.toFocusDuration
import com.charan.unloop.core.utils.DateTimeUtils.toLocale
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.EmptySessions
import com.charan.unloop.presentation.common.components.SectionHeader
import com.charan.unloop.presentation.common.components.SessionListItem
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.stats.components.StatGraph
import com.charan.unloop.presentation.stats.components.StatSummaryTile
import com.charan.unloop.presentation.theme.IndexItem
import com.charan.unloop.presentation.theme.indexItemFor
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    onNavigateToSessionHistory: (LocalDate, LocalDate) -> Unit,
    onNavigateToSessionDetails: (String) -> Unit,
    onNavigateToAppBlockHistory: (LocalDate, LocalDate) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val viewModel = hiltViewModel<StatsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StatsEffect.NavigateToSessionHistory -> onNavigateToSessionHistory(effect.startDate, effect.endDate)
                is StatsEffect.NavigateToAppBlockHistory -> onNavigateToAppBlockHistory(effect.startDate, effect.endDate)
                is StatsEffect.NavigateToSessionDetails -> onNavigateToSessionDetails(effect.id)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomMediumTopBar(
                title = stringResource(R.string.stats),
                scrollBehavior = scrollBehavior,
                showBackButton = false
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding.toScreenContentPadding()),
        ) {
            if (maxWidth >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        statsOverview(state, viewModel::onEvent)
                    }
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        recentSessions(state, viewModel::onEvent)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    statsOverview(state, viewModel::onEvent)
                    recentSessions(state, viewModel::onEvent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyListScope.statsOverview(
    state: StatsState,
    onEvent: (StatsEvent) -> Unit,
) {
    item {
        StatsPeriodSelector(
            selected = state.selectedPeriod,
            onSelect = { type ->
                if (type != state.selectedPeriod) {
                    onEvent(StatsEvent.OnPeriodChange(type))
                }
            }
        )
    }
    item {
        val resources = LocalResources.current
        Column {
            PeriodNavigator(
                label = if (state.selectedPeriod == StatsPeriod.MONTHLY) {
                    state.selectedDateRange.first.month.toLocale()
                } else {
                    state.selectedDateRange.toDisplayString()
                },
                onPrevious = { onEvent(StatsEvent.OnDecrementPeriod) },
                onNext = { onEvent(StatsEvent.OnIncrementPeriod) },
                modifier = Modifier.padding(top = 5.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.stats_time_in_focus),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.totalFocusDurationSeconds.toFocusDuration(resources),
                    style = MaterialTheme.typography.displayMediumEmphasized,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 8.dp)
            ) {
                StatGraph(
                    focusChartData = state.focusChartData,
                    period = state.selectedPeriod
                )
            }
        }
    }
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            StatSummaryTile(
                icon = Icons.Rounded.Timer,
                value = state.sessionCount.toString(),
                label = stringResource(R.string.stats_sessions),
                indexItem = IndexItem.FIRST,
                modifier = Modifier.weight(1f),
                onClick = {
                    onEvent(StatsEvent.OnSeeAllSessions)
                }
            )
            StatSummaryTile(
                icon = Icons.Rounded.Block,
                value = state.appBlockCount.toString(),
                label = stringResource(R.string.stats_app_blocks),
                indexItem = IndexItem.LAST,
                modifier = Modifier.weight(1f),
                onClick = {
                    onEvent(StatsEvent.OnSeeAllAppBlocks)
                }
            )
        }
    }
}

private fun LazyListScope.recentSessions(
    state: StatsState,
    onEvent: (StatsEvent) -> Unit,
) {
    item {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionHeader(title = stringResource(R.string.stats_recent_sessions))
            TextButton(onClick = { onEvent(StatsEvent.OnSeeAllSessions) }) {
                Text(stringResource(R.string.sessions_see_all))
            }
        }
    }

    if (state.recentSessions.isEmpty()) {
        item {
            EmptySessions()
        }
    } else {
        itemsIndexed(
            items = state.recentSessions,
            key = { _, session -> session.id }
        ) { index, session ->
            SessionListItem(
                is24Hour = state.is24Hour,
                sessionName = session.sessionName,
                startTime = session.startTime,
                endTime = session.endTime,
                focusDuration = session.focusDurationSeconds,
                indexItem = state.recentSessions.indexItemFor(index),
                onClick = { onEvent(StatsEvent.OnSessionDetails(session.id)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatsPeriodSelector(
    selected: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        StatsPeriod.entries.forEachIndexed { index, type ->
            ToggleButton(
                checked = type == selected,
                onCheckedChange = { onSelect(type) },
                modifier = Modifier.weight(1f),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    StatsPeriod.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                Text(stringResource(type.toLabelRes()))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PeriodNavigator(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            shapes = IconButtonDefaults.shapes()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.stats_previous_period)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMediumEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onNext,
            shapes = IconButtonDefaults.shapes()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(R.string.stats_next_period)
            )
        }
    }
}

private fun StatsPeriod.toLabelRes(): Int = when (this) {
    StatsPeriod.WEEKLY -> R.string.stats_weekly
    StatsPeriod.MONTHLY -> R.string.stats_monthly
}
