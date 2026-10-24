package com.charan.unloop.presentation.common.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.charan.unloop.ui.theme.IndexItem
import com.charan.unloop.ui.theme.customListItemShapes

@Composable
fun CustomSegmentedListItem(
    modifier: Modifier = Modifier,
    headLineContent: @Composable () -> Unit,
    supportingContent : @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    leadingContent : @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contentPadding : PaddingValues = PaddingValues(16.dp),
    isSelected : Boolean = false,
    indexItem: IndexItem,
    verticalAlignment: Alignment.Vertical = ListItemDefaults.verticalAlignment()

) {
    SegmentedListItem(

        modifier = modifier.padding(1.dp),
        content = headLineContent,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        leadingContent = leadingContent,
        onClick = { onClick?.invoke() },
        contentPadding = contentPadding,
        selected = isSelected,
        verticalAlignment = verticalAlignment,
        shapes = customListItemShapes(indexItem),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),

    )

}