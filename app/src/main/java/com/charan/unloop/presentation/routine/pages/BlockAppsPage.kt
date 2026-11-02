package com.charan.unloop.presentation.routine.pages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.charan.unloop.presentation.common.components.AppsList
import com.charan.unloop.presentation.common.models.AppInfoUiModel

@Composable
fun BlockAppsPage(
    appsList: List<AppInfoUiModel>,
    onAppSelect: (String) -> Unit,
    onSelectAllApps: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
) {
    AppsList(
        modifier = modifier.fillMaxSize(),
        apps = appsList,
        onAppSelect = onAppSelect,
        onAllAppsSelect = onSelectAllApps,
        contentPaddingValues = contentPadding
    )
}
