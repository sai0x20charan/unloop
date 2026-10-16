package com.charan.unloop.presentation.routine.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.presentation.routine.InstalledAppsUiModel
import com.charan.unloop.presentation.routine.components.RoutineLazyColumn
import com.charan.unloop.ui.theme.indexItemFor

@Composable
fun BlockAppsPage(
    appsList: List<InstalledAppsUiModel>,
    onAppSelect: (String) -> Unit,
    onSelectAllApps: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
) {
    val selectedCount = appsList.count { it.isSelected }
    val selectionState = when {
        selectedCount == 0 -> ToggleableState.Off
        selectedCount == appsList.size -> ToggleableState.On
        else -> ToggleableState.Indeterminate
    }

    RoutineLazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        item(key = "select_all_apps") {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .triStateToggleable(
                            state = selectionState,
                            enabled = appsList.isNotEmpty(),
                            role = Role.Checkbox,
                            onClick = onSelectAllApps
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Select all apps", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "$selectedCount of ${appsList.size} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TriStateCheckbox(
                        state = selectionState,
                        onClick = null,
                        enabled = appsList.isNotEmpty()
                    )
                }
            }
        }
        items(appsList.size) { index ->
            val app = appsList[index]
            CustomSegmentedListItem(
                headLineContent = { Text(app.appName) },
                leadingContent = {
                    Image(
                        bitmap = app.icon?.toBitmap()?.asImageBitmap() ?: ImageBitmap(40, 40),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                },
                trailingContent = {
                    if (app.isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                indexItem = appsList.indexItemFor(index),
                onClick = { onAppSelect(app.packageName) },
                isSelected = app.isSelected
            )
        }
    }
}
