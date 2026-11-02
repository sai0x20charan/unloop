package com.charan.unloop.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.charan.unloop.presentation.blocked_apps.BlockedAppsScreen
import com.charan.unloop.presentation.on_boarding.OnBoardingScreen
import com.charan.unloop.presentation.routine.RoutineScreen
import com.charan.unloop.presentation.session_details.SessionDetailsScreen
import com.charan.unloop.presentation.sessions.SessionsScreen
import com.charan.unloop.presentation.settings.aboutlibraries.AboutLibrariesScreen

private val navigationAnimationSpec = tween<IntOffset>(
    durationMillis = 300,
    easing = LinearOutSlowInEasing
)

@Composable
fun RootNavigation(
    isOnBoardingCompleted : Boolean = false
) {
    val backStack = rememberNavBackStack(Destination.BottomBarNav)

    LaunchedEffect(isOnBoardingCompleted) {
        if (!isOnBoardingCompleted) {
            backStack.clear()
            backStack.add(Destination.OnBoarding)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(

            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),

        transitionSpec = {
            (fadeIn() +slideIntoContainer(
                towards = SlideDirection.Start,
                initialOffset = { 300 },
                animationSpec = navigationAnimationSpec
            )) togetherWith (fadeOut() +  slideOutOfContainer(
                towards = SlideDirection.Start,
                targetOffset = { -300 },
                animationSpec = navigationAnimationSpec
            ))
        },
        popTransitionSpec = {
            (fadeIn() + slideIntoContainer(
                towards = SlideDirection.End,
                initialOffset = { -300 },
                animationSpec = navigationAnimationSpec
            )) togetherWith (fadeOut() + slideOutOfContainer(
                towards = SlideDirection.End,
                targetOffset = { 300 },
                animationSpec = navigationAnimationSpec
            ))
        },
        predictivePopTransitionSpec = {
            (fadeIn() + slideIntoContainer(
                towards = SlideDirection.End,
                initialOffset = { -300 },
                animationSpec = navigationAnimationSpec
            )) togetherWith (fadeOut() + slideOutOfContainer(
                towards = SlideDirection.End,
                targetOffset = { 300 },
                animationSpec = navigationAnimationSpec
            ))
        },
        entryProvider = { key ->
            when (key) {

                is Destination.OnBoarding -> NavEntry(key){
                    OnBoardingScreen(onComplete = {
                        backStack.clear()
                        backStack.add(Destination.BottomBarNav)
                    })
                }
                is Destination.BottomBarNav -> NavEntry(key) {
                    BottomBarNavigation(
                        onNavigateToCreateRoutine = {
                            backStack.add(Destination.Routine(it))
                        },
                        onNavigateToLicense = {
                            backStack.add(Destination.AboutLibraries)
                        },
                        onNavigateToSessions = { startDate, endDate ->
                            backStack.add(Destination.Sessions(startDate, endDate))
                        },
                        onNavigateToBlockedApps = { startDate, endDate ->
                            backStack.add(Destination.BlockedApps(startDate, endDate))
                        },
                        onNavigateToSessionDetails = { sessionId ->
                            backStack.add(Destination.SessionDetails(sessionId))
                        }
                    )
                }

                is Destination.AboutLibraries -> NavEntry(key){
                    AboutLibrariesScreen(
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                is Destination.Sessions -> NavEntry(key) {
                    SessionsScreen(
                        startDate = key.startDate,
                        endDate = key.endDate,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToSessionDetails = { sessionId ->
                            backStack.add(Destination.SessionDetails(sessionId))
                        }
                    )
                }

                is Destination.BlockedApps -> NavEntry(key) {
                    BlockedAppsScreen(
                        startDate = key.startDate,
                        endDate = key.endDate,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                is Destination.Routine -> NavEntry(key) {
                    RoutineScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
                        onRoutineCreated = {
                            backStack.removeLastOrNull()
                        },
                        routineId = key.routineId
                    )
                }

                is Destination.SessionDetails -> NavEntry(key) {
                    SessionDetailsScreen(
                        sessionId = key.id,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                else -> throw IllegalArgumentException("Unknown destination: $key")
            }
        }
    )
}
