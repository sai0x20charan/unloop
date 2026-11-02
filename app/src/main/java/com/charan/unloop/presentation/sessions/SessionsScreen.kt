package com.charan.unloop.presentation.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.SectionHeader
import com.charan.unloop.presentation.common.components.SessionListItem
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.theme.indexItemFor
import kotlinx.datetime.LocalDate

@Composable
fun SessionsScreen(
    startDate: LocalDate,
    endDate: LocalDate,
    onBack: () -> Unit,

) {
    val viewModel : SessionsViewModel = hiltViewModel<SessionsViewModel, SessionsViewModel.Factory>(
    creationCallback = { factory -> factory.create(startDate, endDate) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SessionsEffect.NavigateBack -> onBack()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomMediumTopBar(
                title = stringResource(R.string.stats_sessions),
                onBackClick = { viewModel.onEvent(SessionsEvent.OnBack) },
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
            itemsIndexed(
                items = state.sessions,
                key = { _, session -> session.id }
            ) { index, session ->
                SessionListItem(
                    sessionName = session.sessionName,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    focusDuration = session.focusDurationSeconds,
                    indexItem = state.sessions.indexItemFor(index)
                )
            }
        }
    }
}
