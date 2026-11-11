package com.charan.unloop.presentation.common.components

import com.charan.unloop.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

@Composable
fun CustomMediumTopBar(
    title: String,
    onBackClick: () -> Unit = {},
    showBackButton: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior,
    showCloseIcon: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    MediumTopAppBar(
        title = {
            Text(
                text = title,
            )
        },
        navigationIcon = {
            if (showBackButton) {
                BackButton(
                    onBackClick = onBackClick,
                    showCloseIcon = showCloseIcon,
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun CustomTopBar(
    title: String,
    onBackClick: () -> Unit = {},
    showBackButton: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior,
    showCloseIcon: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
            )
        },
        navigationIcon = {
            if (showBackButton) {
                BackButton(
                    onBackClick = onBackClick,
                    showCloseIcon = showCloseIcon,
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun CustomCenterAlignTopBar(
    title: String,
    onBackClick: () -> Unit = {},
    showBackButton: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior,
    showCloseIcon: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
            )
        },
        navigationIcon = {
            if (showBackButton) {
                BackButton(
                    onBackClick = onBackClick,
                    showCloseIcon = showCloseIcon,
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun BackButton(
    onBackClick: () -> Unit,
    showCloseIcon: Boolean = false,
) {
    IconButton(onClick = onBackClick) {
        Icon(
            imageVector = if (showCloseIcon) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(
                if (showCloseIcon) R.string.action_close else R.string.action_back
            )
        )
    }
}
