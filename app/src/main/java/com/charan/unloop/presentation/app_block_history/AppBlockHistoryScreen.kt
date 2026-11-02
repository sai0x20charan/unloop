package com.charan.unloop.presentation.app_block_history

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.presentation.common.components.BlockedAppListItem
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.EmptyBlockedApps
import com.charan.unloop.presentation.common.components.SectionHeader
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.theme.indexItemFor
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppBlockHistoryScreen(
    startDate: LocalDate,
    endDate: LocalDate,
    onBack: () -> Unit,
) {
    val viewModel: AppBlockHistoryViewModel = hiltViewModel<AppBlockHistoryViewModel, AppBlockHistoryViewModel.Factory>(
        creationCallback = { factory -> factory.create(startDate, endDate) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AppBlockHistoryEffect.NavigateBack -> onBack()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomMediumTopBar(
                title = stringResource(R.string.stats_app_blocks),
                onBackClick = { viewModel.onEvent(AppBlockHistoryEvent.OnBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding.toScreenContentPadding()
        ) {
            item {
                state.dateRange?.let { range ->
                    SectionHeader(
                        title = stringResource(
                            R.string.stats_date_range,
                            range.first.toDisplayString(),
                            range.second.toDisplayString()
                        ),
                    )
                }
            }

            if (state.blockedApps.isEmpty()) {
                item {
                    EmptyBlockedApps()
                }
            } else {
                itemsIndexed(
                    items = state.blockedApps,
                    key = { _, item -> item.packageName }
                ) { index, item ->
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
}
