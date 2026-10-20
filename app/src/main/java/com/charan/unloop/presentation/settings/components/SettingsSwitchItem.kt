package com.charan.unloop.presentation.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.ui.theme.IndexItem

@Composable
fun SettingsSwitchItem(
    title: String,
    index: IndexItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    CustomSegmentedListItem(
        modifier = modifier.padding(1.dp),
        indexItem = index,
        headLineContent = {
            Text(title)
        },
        trailingContent = {
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        },
        leadingContent = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null
            )
        },
        onClick = {
            onCheckedChange(!isChecked)
        }
    )
}
