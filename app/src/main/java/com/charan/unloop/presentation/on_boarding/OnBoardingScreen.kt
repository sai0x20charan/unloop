package com.charan.unloop.presentation.on_boarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charan.unloop.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    onComplete : () -> Unit = {},
) {
    val viewModel = hiltViewModel<OnBoardingViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { state.totalPages })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect {
                viewModel.onEvent(OnBoardingEvent.OnPageChange(it))
            }
    }
    val notificationPermission = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS,
        onPermissionResult = { granted ->
            viewModel.onEvent(OnBoardingEvent.OnCheckAllPermissionStatus)
        },
    )

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(OnBoardingEvent.OnCheckAllPermissionStatus)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnBoardingEffect.RequestNotificationPermission -> {
                    notificationPermission.launchPermissionRequest()
                }

                OnBoardingEffect.Complete -> onComplete()
                is OnBoardingEffect.ScrollToPage -> {
                    pagerState.animateScrollToPage(effect.page)
                }
            }
        }
    }



    val permissionItems = listOf(
        OnBoardingPermissionItem(
            title = stringResource(R.string.onboarding_notifications_title),
            description = stringResource(R.string.onboarding_notifications_description),
            icon = Icons.Rounded.NotificationsNone,
            onRequest = {
                viewModel.onEvent(OnBoardingEvent.OnRequestNotificationPermission(showRationale = notificationPermission.status.shouldShowRationale))

            },
            isGranted = state.isNotificationPermissionGranted

        ),
        OnBoardingPermissionItem(
            title = stringResource(R.string.onboarding_overlay_title),
            description = stringResource(R.string.onboarding_overlay_description),
            icon = Icons.Rounded.Layers,
            onRequest = {
                viewModel.onEvent(OnBoardingEvent.OnRequestDisplayOverAppsPermission)
            },
            isGranted = state.isDisplayOverAppsPermissionGranted
        ),
        OnBoardingPermissionItem(
            title = stringResource(R.string.onboarding_usage_title),
            description = stringResource(R.string.onboarding_usage_description),
            icon = Icons.Rounded.QueryStats,
            onRequest = {
                viewModel.onEvent(OnBoardingEvent.OnRequestUsageStatsPermission)
            },
            isGranted = state.isUsageStatsPermissionGranted
        )
    )


    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,

        bottomBar = {
            OnBoardingBottomBar(
                currentPage = pagerState.currentPage,
                pageCount = state.totalPages,
                allGranted = state.allPermissionsGranted,
                onBackClick = {
                    viewModel.onEvent(OnBoardingEvent.OnPreviousPage)
                },
                onNextClick = {
                    viewModel.onEvent(OnBoardingEvent.OnNextPage)
                },
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            key = { it },
        ) { page ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (page) {
                    0 -> WelcomePage(Modifier.fillMaxSize())
                    1-> PermissionsPage(
                        permissionItems = permissionItems,
                        modifier = Modifier.fillMaxSize()
                    )

                }
            }
        }
    }


}

@Composable
private fun WelcomePage(modifier: Modifier = Modifier) {
    LazyColumn (
        modifier = modifier

            ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item {

            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialShapes.Cookie9Sided.toShape()
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.unloop_logo),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Spend your time where you want to.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

        }
    }
}


@Composable
private fun PermissionsPage(
    permissionItems: List<OnBoardingPermissionItem>,
    modifier: Modifier = Modifier,
) {
    val grantedCount = permissionItems.count { it.isGranted }
    val badgeShapes = listOf(
        MaterialShapes.Cookie6Sided,
        MaterialShapes.Clover4Leaf,
        MaterialShapes.Flower,
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            PermissionsHeader(
                grantedCount = grantedCount,
                totalCount = permissionItems.size,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        itemsIndexed(permissionItems, key = { _, it -> it.title }) { index, permission ->
            PermissionItem(
                permission = permission,
                badgeShape = badgeShapes[index % badgeShapes.size].toShape(),
            )
        }
    }
}

@Composable
private fun PermissionsHeader(
    grantedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = if (totalCount == 0) 0f else grantedCount / totalCount.toFloat(),
        label = "permissionProgress"
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {

        Text(
            text = "Permissions",
            style = MaterialTheme.typography.displaySmallEmphasized,
        )
        Text(
            text = "$grantedCount of $totalCount allowed",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun PermissionItem(
    permission: OnBoardingPermissionItem,
    badgeShape: Shape,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (permission.isGranted)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "cardColor"
    )
    val badgeColor by animateColorAsState(
        targetValue = if (permission.isGranted)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.tertiaryContainer,
        label = "badgeColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (permission.isGranted)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onTertiaryContainer,
        label = "iconTint"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                stateDescription = if (permission.isGranted) "Allowed" else "Not allowed"
            },

        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(color = badgeColor, shape = badgeShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (permission.isGranted) Icons.Rounded.Check else permission.icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = permission.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = permission.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(visible = !permission.isGranted) {
                FilledTonalButton(
                    onClick = permission.onRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(
                        text = "Allow",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnBoardingBottomBar(
    currentPage: Int,
    pageCount: Int,
    allGranted: Boolean,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    val isLastPage = currentPage == pageCount - 1
    val pageDescription = stringResource(R.string.onboarding_page_position, currentPage + 1, pageCount)
    val buttonSize = ButtonDefaults.MediumContainerHeight
    val iconSize = ButtonDefaults.iconSizeFor(buttonSize)
    val iconButtonSize = IconButtonDefaults.mediumContainerSize()
    val iconButtonIconSize = IconButtonDefaults.mediumIconSize

    Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
        Box(Modifier.semantics { contentDescription = pageDescription }) {
            PagerIndicator(currentPage = currentPage, size = pageCount)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(visible = currentPage > 0) {
                FilledTonalIconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(iconButtonSize),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.onboarding_back),
                        modifier = Modifier.size(iconButtonIconSize)
                    )
                }
            }
            Button(
                onClick = onNextClick,
                enabled = !isLastPage || allGranted,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(buttonSize),
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else stringResource(R.string.onboarding_next),
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    modifier = Modifier.padding(ButtonDefaults.ButtonWithIconContentPadding)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
            }
        }

    }

}


@Composable
fun PagerIndicator(
    currentPage: Int,
    size: Int
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        repeat(size) { index ->
            val isSelected = currentPage == index
            val width by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 12.dp,
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = 300f
                ),
                label = "indicatorWidth"
            )

            val color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(12.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

data class OnBoardingPermissionItem(
    val title : String,
    val icon : ImageVector,
    val description : String,
    val isGranted : Boolean,
    val onRequest : () -> Unit
)
