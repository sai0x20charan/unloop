package com.charan.unloop.presentation.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.DateTimeUtils.toFocusDuration
import com.charan.unloop.core.utils.DateTimeUtils.toLocale
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.presentation.common.components.EmptySessions
import com.charan.unloop.presentation.common.components.SectionHeader
import com.charan.unloop.presentation.common.components.SessionListItem
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.stats.components.StatGraph
import com.charan.unloop.presentation.stats.components.StatSummaryTile
import com.charan.unloop.presentation.theme.indexItemFor
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(onNavigateToSessions: (LocalDate, LocalDate) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val viewModel = hiltViewModel<StatsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val currentOnNavigateToSessions by rememberUpdatedState(onNavigateToSessions)

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StatsEffect.NavigateToSessions -> currentOnNavigateToSessions(effect.startDate, effect.endDate)
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
        LazyColumn(
            contentPadding = innerPadding.toScreenContentPadding(bottom = 24.dp)
        ) {
            item {
                StatsPeriodSelector(
                    selected = state.selectedPeriod,
                    onSelect = { type ->
                        if (type != state.selectedPeriod) {
                            viewModel.onEvent(StatsEvent.OnPeriodChange(type))
                        }
                    }
                )
            }

            item {
                Surface(
                    modifier = Modifier.padding(top = 16.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        PeriodNavigator(
                            label = if (state.selectedPeriod == StatsPeriod.MONTHLY) {
                                state.selectedDateRange.first.month.toLocale()
                            } else {
                                state.selectedDateRange.toDisplayString()
                            },
                            onPrevious = { viewModel.onEvent(StatsEvent.OnDecrementPeriod) },
                            onNext = { viewModel.onEvent(StatsEvent.OnIncrementPeriod) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
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
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatSummaryTile(
                        icon = Icons.Rounded.Timer,
                        value = state.sessionCount.toString(),
                        label = stringResource(R.string.stats_sessions),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryTile(
                        icon = Icons.Rounded.Block,
                        value = state.appBlockCount.toString(),
                        label = stringResource(R.string.stats_app_blocks),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionHeader(title = stringResource(R.string.stats_recent_sessions))
                    TextButton(onClick = { viewModel.onEvent(StatsEvent.OnSeeAllSessions) }) {
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
                        sessionName = session.sessionName,
                        startTime = session.startTime,
                        endTime = session.endTime,
                        focusDuration = session.focusDurationSeconds,
                        indexItem = state.recentSessions.indexItemFor(index)
                    )
                }
            }
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
        FilledTonalIconButton(
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
        FilledTonalIconButton(
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
