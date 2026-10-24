package com.charan.unloop.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.presentation.common.components.SectionHeader
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.settings.components.SettingsSwitchItem
import com.charan.unloop.presentation.settings.components.ThemeOptionButtonGroup
import com.charan.unloop.ui.theme.IndexItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToLicense : () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SettingsEffect.OnBack -> onNavigateBack()
                SettingsEffect.OnNavigateToOpenSourceLicenses -> onNavigateToLicense()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomMediumTopBar(
                title = stringResource(R.string.settings),
                scrollBehavior = scrollBehavior,
                showBackButton = false,
                onBackClick = {
                    viewModel.onEvent(SettingsEvent.OnBack)
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding.toScreenContentPadding()
        ) {
            item {
                SectionHeader(
                    title = stringResource(R.string.appearance)
                )

                CustomSegmentedListItem(
                    indexItem = IndexItem.FIRST,
                    headLineContent = {
                        Text(stringResource(R.string.theme))
                    },
                    supportingContent = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ThemeOptionButtonGroup(
                                selectedTheme = state.theme
                            ) {
                                viewModel.onEvent(SettingsEvent.OnThemeChange(it))
                            }
                        }
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Contrast,
                            contentDescription = stringResource(R.string.theme)
                        )
                    }
                )

                SettingsSwitchItem(
                    title = stringResource(R.string.use_dynamic_color),
                    index = IndexItem.LAST,
                    isChecked = state.isDynamicColorsEnabled,
                    onCheckedChange = {
                        viewModel.onEvent(SettingsEvent.OnDynamicColorsChange(it))
                    },
                    leadingIcon = Icons.Rounded.ColorLens
                )
            }

            item {
                SectionHeader(
                    title = "About"
                )
                CustomSegmentedListItem(
                    headLineContent = {
                        Text("Open Source Libraries")
                    },
                    indexItem = IndexItem.FIRST,
                    onClick = {
                        viewModel.onEvent(SettingsEvent.OnOpenSourceLicensesClick)
                    }
                )
                CustomSegmentedListItem(
                    headLineContent = {
                        Text("App Version")
                    },
                    indexItem = IndexItem.LAST,
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Code,
                            contentDescription = "App Version"
                        )
                    },
                    trailingContent = {
                        Text(
                            text = state.appVersion,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                )
            }
        }
    }
}
