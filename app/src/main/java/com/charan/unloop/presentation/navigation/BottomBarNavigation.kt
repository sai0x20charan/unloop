package com.charan.unloop.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.charan.unloop.R
import com.charan.unloop.presentation.all_routines.AllRoutinesScreen
import com.charan.unloop.presentation.home.HomeScreen
import com.charan.unloop.presentation.settings.SettingsScreen
import com.charan.unloop.presentation.stats.StatsScreen
import kotlinx.datetime.LocalDate

private enum class MainTab(
    @param:StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val destination: BottomBarNavDestinations,
) {
    Home(
        R.string.navigation_home,
        Icons.Rounded.Home,
        Icons.Outlined.Home,
        BottomBarNavDestinations.Home,
    ),
    Routines(
        R.string.navigation_routines,
        Icons.Rounded.Schedule,
        Icons.Outlined.Schedule,
        BottomBarNavDestinations.AllRoutines,
    ),

    Stats(
        R.string.stats,
        Icons.Rounded.BarChart,
        Icons.Outlined.BarChart,
        BottomBarNavDestinations.Stats,
    ),
    Settings(
        R.string.settings,
        Icons.Rounded.Settings,
        Icons.Outlined.Settings,
        BottomBarNavDestinations.Settings,
    ),
}

@Composable
fun BottomBarNavigation(
    onNavigateToCreateRoutine: (String?) -> Unit,
    onNavigateToLicense: () -> Unit,
    onNavigateToSessions: (LocalDate, LocalDate) -> Unit
) {
    val bottomBarBackStack = rememberNavBackStack(BottomBarNavDestinations.Home)
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var previousSelectedItem by rememberSaveable { mutableIntStateOf(0) }
    val navigationSuiteType = NavigationSuiteScaffoldDefaults.navigationSuiteType(
        currentWindowAdaptiveInfo()
    )

    val (entryAnimation, exitAnimation) = remember(
        selectedItem,
        previousSelectedItem,
        navigationSuiteType,
    ) {
        val forward = selectedItem > previousSelectedItem
        val enter = when (navigationSuiteType) {
            NavigationSuiteType.ShortNavigationBarCompact,
            NavigationSuiteType.ShortNavigationBarMedium -> slideInHorizontally(
                initialOffsetX = { if (forward) it else -it },
                animationSpec = tween(250, easing = LinearOutSlowInEasing)
            )
            NavigationSuiteType.WideNavigationRailCollapsed -> slideInVertically(
                initialOffsetY = { if (forward) it else -it },
                animationSpec = tween(250, easing = LinearOutSlowInEasing)
            )
            else -> fadeIn(tween(250))
        }
        val exit = when (navigationSuiteType) {
            NavigationSuiteType.ShortNavigationBarCompact,
            NavigationSuiteType.ShortNavigationBarMedium -> slideOutHorizontally(
                targetOffsetX = { if (forward) -it else it },
                animationSpec = tween(250, easing = LinearOutSlowInEasing)
            )
            NavigationSuiteType.WideNavigationRailCollapsed -> slideOutVertically(
                targetOffsetY = { if (forward) -it else it },
                animationSpec = tween(250, easing = LinearOutSlowInEasing)
            )
            else -> fadeOut(tween(250))
        }
        enter to exit
    }

    LaunchedEffect(selectedItem) {
        bottomBarBackStack.clear()
        bottomBarBackStack.add(MainTab.entries[selectedItem].destination)
    }

    NavigationSuiteScaffold(
        navigationSuiteType = navigationSuiteType,
        navigationItemVerticalArrangement = Arrangement.Center,
        navigationItems = {
            MainTab.entries.forEachIndexed { index, tab ->
                val selected = selectedItem == index
                NavigationSuiteItem(
                    navigationSuiteType = navigationSuiteType,
                    selected = selected,
                    onClick = {
                        previousSelectedItem = selectedItem
                        selectedItem = index
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(tab.label)) },
                )
            }
        },
    ) {
        NavDisplay(
            backStack = bottomBarBackStack,
            onBack = {
                if (bottomBarBackStack.size > 1) {
                    bottomBarBackStack.removeLastOrNull()
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                entryAnimation togetherWith exitAnimation
            },
            entryProvider = { key ->
                when (key) {
                    BottomBarNavDestinations.Home -> NavEntry(key) {
                        HomeScreen()
                    }
                    BottomBarNavDestinations.AllRoutines -> NavEntry(key) {
                        AllRoutinesScreen(
                            onNavigateToCreateRoutine = onNavigateToCreateRoutine
                        )
                    }
                    BottomBarNavDestinations.Settings -> NavEntry(key) {
                        SettingsScreen(
                            onNavigateToLicense = onNavigateToLicense
                        )
                    }

                    BottomBarNavDestinations.Stats -> NavEntry(key) {
                        StatsScreen(onNavigateToSessions = onNavigateToSessions)
                    }
                    else -> throw IllegalArgumentException("Unknown bottom bar destination: $key")
                }
            }
        )
    }
}
