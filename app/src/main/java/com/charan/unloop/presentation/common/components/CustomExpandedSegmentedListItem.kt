package com.charan.unloop.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.charan.unloop.R
import com.charan.unloop.presentation.theme.IndexItem
import com.charan.unloop.presentation.theme.customListItemShapes

@Composable
fun CustomExpandedSegmentedListItem(
    headLineContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    indexItem: IndexItem,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    headerRole: Role = Role.Button,
    verticalAlignment: Alignment.Vertical = ListItemDefaults.verticalAlignment(),
) {
    val expansionDescription = stringResource(
        if (isExpanded) R.string.section_expanded else R.string.section_collapsed
    )
    val headerModifier = if (headerRole == Role.Switch) {
        Modifier.toggleable(
            value = isExpanded,
            role = Role.Switch,
            onValueChange = { onExpandToggle() },
        )
    } else {
        Modifier
            .clickable(role = Role.Button, onClick = onExpandToggle)
            .semantics { stateDescription = expansionDescription }
    }

    Surface(
        modifier = modifier.fillMaxWidth().padding(1.dp),
        shape = customListItemShapes(indexItem).shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(headerModifier)
                    .minimumInteractiveComponentSize()
                    .padding(contentPadding),
                verticalAlignment = verticalAlignment,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                leadingContent?.invoke()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    headLineContent()
                    supportingContent?.invoke()
                }
                if (trailingContent != null) {
                    trailingContent()
                } else {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                ) {
                    expandedContent()
                }
            }
        }
    }
}
