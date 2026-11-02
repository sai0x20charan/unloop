package com.charan.unloop.presentation.stats.components

import androidx.compose.foundation.Indication
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.charan.unloop.presentation.theme.IndexItem
import com.charan.unloop.presentation.theme.customListItemShapes
import com.charan.unloop.presentation.theme.customListItemShapesForHorizontal

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatSummaryTile(
    icon: ImageVector,
    value: String,
    label: String,
    indexItem: IndexItem,
    modifier: Modifier = Modifier,
    onClick : ()-> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier.indication(
            interactionSource = interactionSource,
            indication = ripple(
                enablePressIndication = true,


            )
        ),
        shape = customListItemShapesForHorizontal(indexItem).shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = onClick,
    ) {

        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLargeEmphasized
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

    }
}
