package com.charan.unloop.presentation.routine

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.presentation.common.mapper.toFormattedDays
import com.charan.unloop.presentation.common.mapper.toFormattedTime
import com.charan.unloop.presentation.common.components.BlockedAppsSetting
import com.charan.unloop.presentation.common.components.CustomExpandedSegmentedListItem
import com.charan.unloop.presentation.common.components.LockSessionControlsSetting
import com.charan.unloop.presentation.common.components.WaitTimeSetting
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.SelectAppsSheet
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.common.showToast
import com.charan.unloop.presentation.routine.components.RoutineScheduleContent
import com.charan.unloop.presentation.routine.components.TimePickerDialog
import com.charan.unloop.presentation.theme.IndexItem

@Composable
fun RoutineScreen(
    onBackClick: () -> Unit = {},
    onRoutineCreated: () -> Unit = {},
    routineId: String? = null,
) {
    val viewModel = hiltViewModel<RoutineViewModel, RoutineViewModel.Factory>(
        creationCallback = { factory -> factory.create(routineId) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val routine = state.routine
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val selectedAppCount = state.installedApps.count { it.isSelected }
    val isWaitEnabled = routine.unlockChallengeType == UnlockChallengeType.WAIT_TIME

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RoutineEffect.OnBack -> onRoutineCreated()
                is RoutineEffect.ShowToast -> {
                    context.showToast(effect.message)
                }
            }
        }
    }

    if (state.showStartTimeDialog) {
        TimePickerDialog(
            title = stringResource(R.string.routine_select_start_time),
            initialHour = routine.startTime.hour,
            initialMinute = routine.startTime.minute,
            onConfirm = { hour, minute ->
                viewModel.onEvent(RoutineEvent.OnStartTimeChange(hour, minute))
            },
            onDismiss = { viewModel.onEvent(RoutineEvent.OnStartTimeToggle) },
        )
    }
    if (state.showEndTimeDialog) {
        TimePickerDialog(
            title = stringResource(R.string.routine_select_end_time),
            initialHour = routine.endTime.hour,
            initialMinute = routine.endTime.minute,
            onConfirm = { hour, minute ->
                viewModel.onEvent(RoutineEvent.OnEndTimeChange(hour, minute))
            },
            onDismiss = { viewModel.onEvent(RoutineEvent.OnEndTimeToggle) },
        )
    }
    if (state.showAppsSheet) {
        SelectAppsSheet(
            apps = state.installedApps,
            onAppSelect = { viewModel.onEvent(RoutineEvent.OnInstalledAppSelect(it)) },
            onAllAppsSelect = { viewModel.onEvent(RoutineEvent.OnSelectAllAppsClick) },
            onClose = { viewModel.onEvent(RoutineEvent.OnAppsSheetToggle) },
        )
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
           ,
        topBar = {
            CustomMediumTopBar(
                title = stringResource(if (state.isEditing) R.string.routine_edit else R.string.routine_create),
                onBackClick = onBackClick,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onEvent(RoutineEvent.OnCreateRoutineClick)
                },
                enabled = !state.isSaving,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(ButtonDefaults.MediumContainerHeight),
            ) {
                Text(
                    text = stringResource(
                        when {
                            state.isSaving -> R.string.routine_saving
                            state.isEditing -> R.string.routine_save
                            else -> R.string.routine_create
                        }
                    )
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding.toScreenContentPadding(bottom = 16.dp),
        ) {
            item(key = "name") {
                OutlinedTextField(
                    value = routine.name,
                    onValueChange = { viewModel.onEvent(RoutineEvent.OnRoutineNameChange(it)) },
                    label = { Text(stringResource(R.string.routine_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                )
            }
            item(key = "schedule") {
                CustomExpandedSegmentedListItem(
                    indexItem = IndexItem.FIRST,
                    headLineContent = { Text(stringResource(R.string.routine_step_schedule)) },
                    supportingContent = {
                        Text(
                            text = stringResource(
                                R.string.routine_list_metadata,
                                routine.toFormattedDays(),
                                routine.toFormattedTime(),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingContent = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
                    isExpanded = state.isScheduleExpanded,
                    onExpandToggle = { viewModel.onEvent(RoutineEvent.OnScheduleToggle) },
                    expandedContent = {
                        RoutineScheduleContent(
                             selectedDays = state.routine.selectedDays,
                            startTime = state.routine.startTime,
                            endTime = state.routine.endTime,
                            onEvent = viewModel::onEvent)
                    },
                )
            }
            item(key = "blocked_apps") {
                BlockedAppsSetting(
                    selectedAppCount = selectedAppCount,
                    indexItem = IndexItem.MIDDLE,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onEvent(RoutineEvent.OnAppsSheetToggle)
                    },
                    appIcons = state.installedApps.filter { it.isSelected }.mapNotNull { it.icon },
                )
            }
            item(key = "wait_rule") {
                WaitTimeSetting(
                    isWaitEnabled = isWaitEnabled,
                    waitTimeSeconds = routine.waitDurationSeconds ?: 30,
                    indexItem = IndexItem.MIDDLE,
                    onWaitToggle = {
                        viewModel.onEvent(
                            RoutineEvent.OnUnlockChallengeTypeChange(
                                if (isWaitEnabled) UnlockChallengeType.STRICT_MODE else UnlockChallengeType.WAIT_TIME
                            )
                        )
                    },
                    onWaitTimeChange = { viewModel.onEvent(RoutineEvent.OnWaitTimeSecondsChange(it)) },
                )
            }
            item(key = "session_controls") {
                LockSessionControlsSetting(
                    isLocked = routine.areSessionControlsLocked,
                    indexItem = IndexItem.LAST,
                    description = stringResource(R.string.routine_lock_session_controls_description),
                    onLockedChange = { viewModel.onEvent(RoutineEvent.OnSessionControlsLockedChange(it)) },
                )
            }
        }
    }
}
