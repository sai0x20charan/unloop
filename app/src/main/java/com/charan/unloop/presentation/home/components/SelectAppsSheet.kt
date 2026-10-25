package com.charan.unloop.presentation.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Spacer
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.charan.unloop.R
import com.charan.unloop.presentation.common.components.AppsList
import com.charan.unloop.presentation.common.components.CustomSegmentedListItem
import com.charan.unloop.presentation.common.models.InstalledAppsUiModel
import com.charan.unloop.ui.theme.indexItemFor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SelectAppsSheet(
    apps: List<InstalledAppsUiModel>,
    onAppSelect: (String) -> Unit,
    onAllAppsSelect: () -> Unit,
    onClose: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,

    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.home_apps_to_block),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.home_sheet_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AppsList(
            modifier = Modifier
                .weight(1f,false),
            apps = apps,
            onAppSelect = onAppSelect,
            onAllAppsSelect = onAllAppsSelect,
            contentPaddingValues = PaddingValues(horizontal = 10.dp),
            onStartSearching = {
                scope.launch {
                    sheetState.expand()
                }
            }


        )

        val selectedCount = apps.count { it.isSelected }
            Button(
                onClick = onClose,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(ButtonDefaults.MediumContainerHeight),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight)),
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(
                    text = if (selectedCount == 0) {
                        stringResource(R.string.home_sheet_done_none)
                    } else {
                        pluralStringResource(R.plurals.home_sheet_done, selectedCount, selectedCount)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PresetDurationSelector(
    presets: List<Int>,
    hour: Int,
    minute: Int,
    isHourValid: Boolean,
    isMinutesValid: Boolean,
    onPresetSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (presets.isEmpty()) return
    val currentTotalMinutes = hour * 60 + minute

    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        modifier = modifier.fillMaxWidth(),
    ) {
        presets.forEachIndexed { index, preset ->
            val isChecked = isHourValid && isMinutesValid && currentTotalMinutes == preset
            ToggleButton(
                checked = isChecked,
                onCheckedChange = {
                    onPresetSelect(preset)
                },
                shapes = when {
                    presets.size == 1 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    index == presets.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = formatPreset(preset),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
fun SelectedBlockedAppsCard(
    selectedApps: List<InstalledAppsUiModel>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String = stringResource(R.string.home_apps_to_block),
) {
    val maxPreviewIcons = 3
    val previewApps = remember(selectedApps) { selectedApps.take(maxPreviewIcons) }
    val remainingCount = (selectedApps.size - maxPreviewIcons).coerceAtLeast(0)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selectedApps.isNotEmpty()) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            tint = if (selectedApps.isNotEmpty()) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (selectedApps.isEmpty()) {
                            stringResource(R.string.home_select_apps_hint)
                        } else {
                            pluralStringResource(
                                R.plurals.home_selected_apps,
                                selectedApps.size,
                                selectedApps.size,
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (previewApps.isNotEmpty()) {
                    val avatarSize = 28.dp
                    val overlap = 10.dp
                    val step = avatarSize - overlap
                    val hasRemaining = remainingCount > 0

                    val totalItems = previewApps.size + if (hasRemaining) 1 else 0
                    val totalWidth = avatarSize + step * (totalItems - 1)

                    Box(
                        modifier = Modifier
                            .width(totalWidth)
                            .height(avatarSize)
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                    ) {
                        previewApps.forEachIndexed { index, app ->
                            app.icon?.let { icon ->
                                Image(
                                    bitmap = remember(icon) {
                                        icon.toBitmap().asImageBitmap()
                                    },
                                    contentDescription = app.appName,
                                    modifier = Modifier
                                        .offset(x = step * index)
                                        .size(avatarSize)
                                        .zIndex((totalItems - index).toFloat())
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.surfaceContainer,
                                            CircleShape
                                        )
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

                        if (hasRemaining) {
                            Box(
                                modifier = Modifier
                                    .offset(x = step * previewApps.size)
                                    .size(avatarSize)
                                    .zIndex(0f)
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.surfaceContainer,
                                        CircleShape
                                    )
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$remainingCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (enabled) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private fun formatPreset(minutes: Int): String {
    return when {
        minutes % 60 == 0 -> "${minutes / 60}h"
        minutes < 60 -> "${minutes}m"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DurationInputField(
    value: Int,
    onValueChange: (String) -> Unit,
    isValid: Boolean,
    imeAction: ImeAction,
    boxWidth: Dp,
    focus: FocusRequester,
    modifier: Modifier = Modifier,
    onDone: (() -> Unit)? = null,
) {
    val fontSize by animateFloatAsState(
        targetValue = when {
            value.toString().length <= 2 -> MaterialTheme.typography.displayMedium.fontSize.value
            value.toString().length == 3 -> MaterialTheme.typography.displaySmall.fontSize.value
            else -> MaterialTheme.typography.displaySmall.fontSize.value
        },
        animationSpec = tween(200),
        label = "fontSize",
    )
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme

    val borderColor by animateColorAsState(
        targetValue = when {
            !isValid -> colorScheme.error
            isFocused -> colorScheme.primary
            else -> colorScheme.outlineVariant.copy(alpha = 0.4f)
        },
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "durationBorderColor",
    )

    BasicTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { newValue ->
            onValueChange(newValue)
        },
        modifier = Modifier.focusRequester(focus),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Right) },
            onDone = {
                focusManager.clearFocus()
                onDone?.invoke()
            },
        ),
        textStyle = MaterialTheme.typography.displayLarge.copy(
            fontSize = fontSize.sp,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(width = boxWidth, height = 64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colorScheme.surface)
                    .border(
                        border = BorderStroke(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = borderColor,
                        ),
                        shape = RoundedCornerShape(18.dp),
                    ),
            ) {
                if (value == 0) {
                    Text(
                        text = "00",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
                innerTextField()
            }
        },
    )
}
