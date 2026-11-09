package com.charan.unloop.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.charan.unloop.data.model.TimeFormatOptions

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun TimeFormatOptionButtonGroup(
    modifier: Modifier = Modifier,
    selectedFormat: TimeFormatOptions,
    onSelectFormat: (TimeFormatOptions) -> Unit
) {
    FlowRow(
        modifier = modifier
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TimeFormatOptions.entries.forEachIndexed { index, option ->
            val isSelected = option == selectedFormat

            FilledTonalToggleButton(
                checked = isSelected,
                onCheckedChange = { onSelectFormat(option) },
                modifier = Modifier
                    .weight(1f),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    TimeFormatOptions.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                Text(stringResource(option.toResId()))
            }
        }
    }
}
