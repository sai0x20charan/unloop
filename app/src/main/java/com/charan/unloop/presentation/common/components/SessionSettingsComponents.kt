package com.charan.unloop.presentation.common.components

import android.graphics.drawable.Drawable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.core.graphics.drawable.toDrawable
import com.charan.unloop.R
import com.charan.unloop.presentation.home.components.SelectedAppsPreview
import com.charan.unloop.presentation.theme.IndexItem

@Composable
fun BlockedAppsSetting(
    selectedAppCount: Int,
    onClick: () -> Unit,
    indexItem: IndexItem,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.home_blocked_apps),
    appIcons : List<Drawable>,
) {
    CustomSegmentedListItem(
        modifier = modifier,
        indexItem = indexItem,
        headLineContent = { Text(text = title, style = MaterialTheme.typography.titleSmall) },
        supportingContent = {
            Text(
                text = if (selectedAppCount == 0) {
                    stringResource(R.string.home_select_apps_hint)
                } else {
                    pluralStringResource(R.plurals.home_selected_apps, selectedAppCount, selectedAppCount)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = { Icon(Icons.Rounded.Apps, contentDescription = null) },
        trailingContent = {
            if(appIcons.isNotEmpty()) {
                SelectedAppsPreview(
                    selectedApps = appIcons,
                )
            } else{
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
            }
        },
        onClick = onClick,
    )
}

@Composable
fun WaitTimeSetting(
    isWaitEnabled: Boolean,
    waitDurationSeconds: Int,
    onWaitToggle: () -> Unit,
    onWaitTimeChange: (Int) -> Unit,
    indexItem: IndexItem,
    modifier: Modifier = Modifier,
) {
    CustomExpandedSegmentedListItem(
        modifier = modifier,
        indexItem = indexItem,
        headLineContent = {
            Text(
                text = stringResource(R.string.routine_wait_to_unlock),
                style = MaterialTheme.typography.titleSmall,
            )
        },
        supportingContent = {
            Text(
                text = stringResource(
                    if (isWaitEnabled) R.string.home_wait_enabled_description
                    else R.string.home_wait_disabled_description
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = { Icon(Icons.Rounded.HourglassEmpty, contentDescription = null) },
        trailingContent = { Switch(checked = isWaitEnabled, onCheckedChange = null) },
        headerRole = Role.Switch,
        isExpanded = isWaitEnabled,
        onExpandToggle = onWaitToggle,
        expandedContent = {
            WaitTimeSlider(seconds = waitDurationSeconds, onSecondsChange = onWaitTimeChange)
        },
    )
}

@Composable
fun LockSessionControlsSetting(
    isLocked: Boolean,
    onLockedChange: (Boolean) -> Unit,
    indexItem: IndexItem,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    CustomSegmentedListItem(
        modifier = modifier,
        indexItem = indexItem,
        headLineContent = { Text(stringResource(R.string.routine_lock_session_controls)) },
        supportingContent = description?.let { text ->
            {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        leadingContent = { Icon(Icons.Rounded.Lock, contentDescription = null) },
        trailingContent = { Switch(checked = isLocked, onCheckedChange = onLockedChange) },
        onClick = { onLockedChange(!isLocked) },
    )
}
