package com.charan.unloop.presentation.all_routines.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.charan.unloop.R
import com.charan.unloop.presentation.all_routines.AllRoutineEvent
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.presentation.theme.IndexItem
import com.charan.unloop.presentation.theme.indexItemFor

@Composable
fun RoutineListItem(
    isEnabled : Boolean = true,
    name : String,
    formattedTime : String,
    blockedAppSize : Int,
    formattedDays : String,
    onEnableSwitchClick : (Boolean) -> Unit,
    onItemClick : () -> Unit,
    indexItem: IndexItem

) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.5f,
        label = "routineContentAlpha"
    )

    CustomSegmentedListItem(
        headLineContent = {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(contentAlpha)
            )
        },
        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .alpha(contentAlpha)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoPill(
                        text = formattedDays,
                        icon = Icons.Outlined.CalendarMonth
                    )
                    if (blockedAppSize > 0) {
                        InfoPill(
                            text = pluralStringResource(
                                R.plurals.routine_apps_blocked,
                                blockedAppSize,
                                blockedAppSize
                            ),
                            icon = Icons.Outlined.Block
                        )
                    }
                }
            }
        },
        trailingContent = {
            Switch(
                checked = isEnabled,
                onCheckedChange = { isChecked ->
                    onEnableSwitchClick(isChecked)
                },
                thumbContent = if (isEnabled) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else null
            )
        },
        indexItem = indexItem,
        isSelected = false,
        onClick = {
           onItemClick()
        }
    )
}

@Composable
private fun InfoPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}