package com.charan.unloop.presentation.all_routines

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.ui.theme.indexItemFor

@Composable
fun AllRoutinesScreen(
    onNavigateToCreateRoutine: () -> Unit = {},
) {
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val viewModel = hiltViewModel<AllRoutinesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AllRoutineEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                AllRoutineEffect.NavigateToCreateRoutine -> {
                    onNavigateToCreateRoutine()
                }

                AllRoutineEffect.NavigateBack -> {

                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            CustomMediumTopBar(
                title = "All Routines",
                showBackButton = false,

                scrollBehavior = scroll
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AllRoutineEvent.OnAddRoutineClick)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Create Routine"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding.toScreenContentPadding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(state.allRoutines.size) { index ->
                val routine = state.allRoutines[index]
                CustomSegmentedListItem(
                    headLineContent = {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "${routine.formattedTime} • ${routine.formattedDays}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (routine.blockedApps.isNotEmpty()) {
                                    Text(
                                        text = "${routine.blockedApps.size} apps blocked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (routine.isIntervalBlocking) {
                                    val intervalText = routine.intervalMinutes?.let { mins ->
                                        val h = mins / 60
                                        val m = mins % 60
                                        if (h > 0 && m > 0) "${h}h ${m}m" else if (h > 0) "${h}h" else "${m}m"
                                    } ?: ""
                                    if (intervalText.isNotEmpty()) {
                                        Text(
                                            text = "Every $intervalText",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    },
                    leadingContent = {
                        Surface(
                            shape = CircleShape,
                            color = if (routine.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (routine.isIntervalBlocking) Icons.Rounded.HourglassTop else Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = if (routine.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Switch(
                                checked = routine.isEnabled,
                                onCheckedChange = { isChecked ->
                                    viewModel.onEvent(
                                        AllRoutineEvent.OnToggleRoutine(routine.id, isChecked)
                                    )
                                }
                            )
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(AllRoutineEvent.OnDeleteClick(routine))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete routine",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    indexItem = state.allRoutines.indexItemFor(index),
                    isSelected = false
                )
            }
        }
    }

}
