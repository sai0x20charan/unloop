package com.charan.unloop.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.charan.unloop.R
import com.charan.unloop.presentation.common.models.InstalledAppsUiModel
import com.charan.unloop.presentation.theme.indexItemFor
import kotlinx.coroutines.launch

@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    apps: List<InstalledAppsUiModel>,
    onAppSelect: (String) -> Unit,
    onAllAppsSelect: () -> Unit,
    contentPaddingValues: PaddingValues = PaddingValues(),
    onStartSearching: () -> Unit = {}

) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val searchInteractionSource = remember { MutableInteractionSource() }
    val isSearchFocused by searchInteractionSource.collectIsFocusedAsState()

    LaunchedEffect(isSearchFocused) {
        if (isSearchFocused) onStartSearching()
    }

    val query = textFieldState.text.toString().trim()
    val filteredApps = remember(apps, query) {
        if (query.isEmpty()) apps else apps.filter { app ->
            app.appName.contains(query, ignoreCase = true)
        }
    }
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            interactionSource = searchInteractionSource,
            onSearch = {
                focusManager.clearFocus()
                scope.launch { searchBarState.animateToCollapsed() }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search") },
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
            },
            trailingIcon = {
                if (textFieldState.text.isNotEmpty()) {
                    IconButton(onClick = { textFieldState.clearText() }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear Search"
                        )
                    }
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPaddingValues,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {

        item(key = "search") {
            Surface(
                shape = SearchBarDefaults.inputFieldShape,
                color = SearchBarDefaults.colors().containerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                content = inputField,
            )
        }
        item(key = "select_all_apps") {
            AnimatedVisibility(
                visible = query.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = 40)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = tween(durationMillis = 120)),
            ) {
                val selectedCount = apps.count { it.isSelected }
                val selectionState = when {
                    apps.isEmpty() || selectedCount == 0 -> ToggleableState.Off
                    selectedCount == apps.size -> ToggleableState.On
                    else -> ToggleableState.Indeterminate
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .triStateToggleable(
                            state = selectionState,
                            enabled = apps.isNotEmpty() && query.isEmpty(),
                            onClick = onAllAppsSelect,
                            role = Role.Checkbox,
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_select_all),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        TriStateCheckbox(
                            state = selectionState,
                            onClick = null,
                            enabled = apps.isNotEmpty() && query.isEmpty(),
                        )
                    }
                }
            }
        }

        if (query.isNotEmpty() && filteredApps.isEmpty()) {
            item(key = "no_search_results") {
                Text(
                    text = "No Results Found",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        itemsIndexed(
            items = filteredApps,
            key = { _, app -> app.packageName },
        ) { index, app ->
            val appBitmap = remember(app.icon) {
                app.icon?.toBitmap()?.asImageBitmap()
            }

            CustomSegmentedListItem(
                headLineContent = {
                    Text(
                        text = app.appName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingContent = {
                    if (appBitmap != null) {
                        Image(
                            bitmap = appBitmap,
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                },
                isSelected = app.isSelected,
                indexItem = filteredApps.indexItemFor(index),
                onClick = {
                    onAppSelect(app.packageName)
                },
            )
        }
    }

}
