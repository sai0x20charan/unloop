package com.charan.stepaway.presentation.routine

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import com.charan.stepaway.presentation.routine.components.*

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.ui.Alignment
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.stepaway.presentation.common.components.CustomMediumTopBar
import com.charan.stepaway.presentation.common.components.toScreenContentPadding
import com.charan.stepaway.presentation.routine.pages.BlockAppsPage
import com.charan.stepaway.presentation.routine.pages.ReviewPage
import com.charan.stepaway.presentation.routine.pages.ScheduleRoutinePage
import com.charan.stepaway.presentation.routine.pages.UnlockChallengePage

@Composable
fun RoutineScreen() {
    val viewModel = hiltViewModel<RoutineViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pagerState = rememberPagerState(
        initialPage = state.pageState,
        pageCount = { state.totalPageState }
    )
    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = context.packageManager.queryIntentActivities(intent, 0)
        Log.d("TAG", "RoutineScreen: $apps")
    }

    LaunchedEffect(state.pageState) {
        if (pagerState.currentPage != state.pageState) {
            pagerState.animateScrollToPage(state.pageState)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    if (state.showStartTimeDialog) {
        TimePickerDialog(
            title = "Select Start Time",
            initialHour = state.routineScheduleState.routineStartTime.hour,
            initialMinute = state.routineScheduleState.routineStartTime.minute,
            onConfirm = { hour, minute ->
                viewModel.onEvent(RoutineEvent.OnStartTimeChange(hour, minute))
            },
            onDismiss = {
                viewModel.onEvent(RoutineEvent.OnStartTimeDialogDismiss)
            }
        )
    }

    if (state.showEndTimeDialog) {
        TimePickerDialog(
            title = "Select End Time",
            initialHour = state.routineScheduleState.routineEndTime.hour,
            initialMinute = state.routineScheduleState.routineEndTime.minute,
            onConfirm = { hour, minute ->
                viewModel.onEvent(RoutineEvent.OnEndTimeChange(hour, minute))
            },
            onDismiss = {
                viewModel.onEvent(RoutineEvent.OnEndTimeDialogDismiss)
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomMediumTopBar(
                title = "Create Routine",
                showBackButton = false,
                onBackClick = {},
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                RoutineBottomBar(
                    isLastPage = pagerState.pageCount - 1 == pagerState.currentPage,
                    showPreviousButton = pagerState.currentPage > 0,
                    onPrevious = {
                        viewModel.onEvent(RoutineEvent.OnBackClick)
                    },
                    onNext = {
                        if (pagerState.currentPage == state.totalPageState - 1) {
                            viewModel.onEvent(RoutineEvent.OnCreateRoutineClick)
                        } else {
                            viewModel.onEvent(RoutineEvent.OnNextClick)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding.toScreenContentPadding(includeBottomInset = false)),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val stepTitle = state.stepTitles.getOrNull(pagerState.currentPage) ?: ""
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Step ${pagerState.currentPage + 1} of ${state.totalPageState}",
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = stepTitle,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = {
                        (pagerState.currentPage + 1).toFloat() / state.totalPageState
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f),
            ) { page ->
                when (page) {
                    0 -> ScheduleRoutinePage(
                        routineName = state.routineName,
                        scheduleState = state.routineScheduleState,
                        onEvent = viewModel::onEvent,
                        contentPadding = innerPadding.toScreenContentPadding(includeTopInset = false, top = 10.dp)
                    )

                    1 -> BlockAppsPage(
                        appsList = state.installedApps,
                        onAppSelect = {
                            viewModel.onEvent(
                                RoutineEvent.OnInstalledAppSelect(
                                    it
                                )
                            )
                        },
                        contentPadding = innerPadding.toScreenContentPadding(includeTopInset = false, top = 10.dp)
                    )

                    2 -> UnlockChallengePage(
                        unlockChallengeType = state.unlockChallengeType,
                        waitTimeSeconds = state.waitTimeSeconds,
                        oneTimeUnlockWaitTimeHours = state.oneTimeUnlockWaitTimeHours,
                        oneTimeUnlockWaitTimeMinutes = state.oneTimeUnlockWaitTimeMinutes,
                        isIntervalBlocking = state.routineScheduleState.isIntervalBlocking,
                        lockDurationHours = state.routineScheduleState.lockDurationHours,
                        lockDurationMinutes = state.routineScheduleState.lockDurationMinutes,
                        onEvent = viewModel::onEvent,
                        contentPadding = innerPadding.toScreenContentPadding(includeTopInset = false, top = 10.dp)
                    )

                    3 -> ReviewPage(
                        routineName = state.routineName,
                        scheduleState = state.routineScheduleState,
                        unlockChallengeType = state.unlockChallengeType,
                        waitTimeSeconds = state.waitTimeSeconds,
                        oneTimeUnlockWaitTimeHours = state.oneTimeUnlockWaitTimeHours,
                        oneTimeUnlockWaitTimeMinutes = state.oneTimeUnlockWaitTimeMinutes,
                        selectedApps = state.installedApps.filter { it.isSelected },
                        contentPadding = innerPadding.toScreenContentPadding(includeTopInset = false, top = 10.dp)
                    )
                }
            }
        }
    }
}

