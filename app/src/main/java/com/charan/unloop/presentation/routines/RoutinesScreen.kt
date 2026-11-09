package com.charan.unloop.presentation.routines

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.presentation.common.mapper.toFormattedDays
import com.charan.unloop.presentation.common.mapper.toFormattedTime
import com.charan.unloop.presentation.routines.components.RoutineListItem
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.common.showToast
import com.charan.unloop.presentation.theme.indexItemFor

@Composable
fun RoutinesScreen(
    onNavigateToRoutineDetails: (String?) -> Unit = {},
) {
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val viewModel = hiltViewModel<RoutinesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RoutinesEffect.ShowToast -> {
                    context.showToast(effect.message)
                }

                is RoutinesEffect.NavigateToRoutineDetails -> {
                    onNavigateToRoutineDetails(effect.routineId)
                }

                RoutinesEffect.NavigateBack -> {

                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            CustomMediumTopBar(
                title = stringResource(R.string.routine_all),
                showBackButton = false,

                scrollBehavior = scroll
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(RoutinesEvent.OnEditRoutineClick(null))
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.routine_create)
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
                RoutineListItem(
                    name = routine.name,
                    formattedTime = routine.toFormattedTime(state.is24Hour),
                    blockedAppSize = routine.blockedAppPackages.size,
                    formattedDays = routine.toFormattedDays(),
                    isEnabled = routine.isEnabled,
                    onEnableSwitchClick = { isChecked ->
                        viewModel.onEvent(RoutinesEvent.OnToggleRoutine(routine.id, isChecked))
                    },
                    onItemClick = {
                        viewModel.onEvent(RoutinesEvent.OnEditRoutineClick(routine.id))
                    },
                    indexItem = state.allRoutines.indexItemFor(index)
                )
            }
        }
    }

}


