package com.charan.unloop.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.charan.unloop.core.utils.launchFeedbackEmail
import com.charan.unloop.core.utils.launchUrl
import com.charan.unloop.presentation.common.components.CustomMediumTopBar
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.presentation.common.components.SectionHeader
import com.charan.unloop.presentation.common.components.toScreenContentPadding
import com.charan.unloop.presentation.settings.components.SettingsSwitchItem
import com.charan.unloop.presentation.settings.components.ThemeOptionButtonGroup
import com.charan.unloop.presentation.settings.components.TimeFormatOptionButtonGroup
import com.charan.unloop.presentation.theme.IndexItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToOpenSourceLicenses : () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(context) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                SettingsEffect.LaunchSendFeedbackEmail -> context.launchFeedbackEmail()
                is SettingsEffect.OpenUrl -> context.launchUrl(effect.url)
                SettingsEffect.OnBack -> onNavigateBack()
                SettingsEffect.OnNavigateToOpenSourceLicenses -> onNavigateToOpenSourceLicenses()
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
                    index = IndexItem.MIDDLE,
                    isChecked = state.isDynamicColorsEnabled,
                    onCheckedChange = {
                        viewModel.onEvent(SettingsEvent.OnDynamicColorsChange(it))
                    },
                    leadingIcon = Icons.Rounded.ColorLens
                )

                CustomSegmentedListItem(
                    indexItem = IndexItem.LAST,
                    headLineContent = { Text(stringResource(R.string.time_format)) },
                    supportingContent = {
                        TimeFormatOptionButtonGroup(selectedFormat = state.timeFormat) {
                            viewModel.onEvent(SettingsEvent.OnTimeFormatChange(it))
                        }
                    },
                    leadingContent = {
                        Icon(Icons.Rounded.Schedule, contentDescription = null)
                    }
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.settings_support)
                )
                CustomSegmentedListItem(
                    indexItem = IndexItem.FIRST,
                    headLineContent = {
                        Text(stringResource(R.string.settings_send_feedback))
                    },
                    onClick = {
                        viewModel.onEvent(SettingsEvent.OnSendFeedbackClick)
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = stringResource(R.string.settings_send_feedback)
                        )
                    }
                )
                CustomSegmentedListItem(
                    indexItem = IndexItem.LAST,
                    headLineContent = {
                        Text(stringResource(R.string.settings_rate_app))
                    },
                    onClick = {
                        viewModel.onEvent(SettingsEvent.OnRateAppClick)
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.google_play),
                            contentDescription = stringResource(R.string.settings_rate_app),
                            modifier = Modifier.size(IconButtonDefaults.mediumIconSize)
                        )
                    }
                )
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.settings_about)
                )
                CustomSegmentedListItem(
                    headLineContent = {
                        Text(stringResource(R.string.settings_open_source_libraries))
                    },
                    indexItem = IndexItem.FIRST,
                    onClick = {
                        viewModel.onEvent(SettingsEvent.OnOpenSourceLicensesClick)
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = stringResource(R.string.settings_open_source_libraries)
                        )
                    }
                )
                CustomSegmentedListItem(
                    indexItem = IndexItem.MIDDLE,
                    headLineContent = {
                        Text(stringResource(R.string.settings_source_code))
                    },
                    onClick = {
                        viewModel.onEvent(SettingsEvent.OnOpenSourceCodeClick)
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Folder,
                            contentDescription = stringResource(R.string.settings_source_code)
                        )
                    }
                )
                CustomSegmentedListItem(
                    headLineContent = {
                        Text(stringResource(R.string.settings_app_version))
                    },
                    indexItem = IndexItem.LAST,
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Code,
                            contentDescription = stringResource(R.string.settings_app_version)
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
