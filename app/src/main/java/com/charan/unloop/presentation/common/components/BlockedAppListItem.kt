package com.charan.unloop.presentation.common.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.charan.unloop.R
import com.charan.unloop.presentation.theme.IndexItem

@Composable
fun BlockedAppListItem(
    appName: String,
    blockedCount: Int,
    indexItem: IndexItem,
    modifier: Modifier = Modifier,
    appIcon: Drawable? = null,
    accessGrantedCount: Int = 0,
) {
    val appBitmap = remember(appIcon) {
        appIcon?.toBitmap()?.asImageBitmap()
    }
    CustomSegmentedListItem(
        modifier = modifier,
        headLineContent = {
            Text(
                text = appName,
                style = MaterialTheme.typography.titleSmall
            )
        },
        supportingContent = {
            if (accessGrantedCount > 0) {
                Text(
                    text = pluralStringResource(
                        R.plurals.session_details_access_grants,
                        accessGrantedCount,
                        accessGrantedCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            if (appBitmap != null) {
                Image(
                    bitmap = appBitmap,
                    contentDescription = appName,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        },
        trailingContent = {
            Text(
                text = pluralStringResource(
                    R.plurals.session_details_blocks,
                    blockedCount,
                    blockedCount
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        indexItem = indexItem
    )
}

@Composable
fun EmptyBlockedApps(
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Block,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.stats_no_blocked_apps),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.stats_no_blocked_apps_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
