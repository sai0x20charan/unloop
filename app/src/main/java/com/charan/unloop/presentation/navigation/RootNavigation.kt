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
import com.charan.unloop.presentation.app_block_history.AppBlockHistoryScreen
import com.charan.unloop.presentation.onboarding.OnboardingScreen
import com.charan.unloop.presentation.routine_details.RoutineDetailsScreen
import com.charan.unloop.presentation.session_details.SessionDetailsScreen
import com.charan.unloop.presentation.session_history.SessionHistoryScreen
import com.charan.unloop.presentation.settings.open_source_licenses.OpenSourceLicensesScreen

private val navigationAnimationSpec = tween<IntOffset>(
    durationMillis = 300,
    easing = LinearOutSlowInEasing
)

@Composable
fun RootNavigation(
    isOnboardingCompleted : Boolean = false
) {
    val backStack = rememberNavBackStack(Destination.BottomBarNav)

    LaunchedEffect(isOnboardingCompleted) {
        if (!isOnboardingCompleted) {
            backStack.clear()
            backStack.add(Destination.Onboarding)
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

                is Destination.Onboarding -> NavEntry(key){
                    OnboardingScreen(onComplete = {
                        backStack.clear()
                        backStack.add(Destination.BottomBarNav)
                    })
                }
                is Destination.BottomBarNav -> NavEntry(key) {
                    BottomBarNavigation(
                        onNavigateToRoutineDetails = {
                            backStack.add(Destination.RoutineDetails(it))
                        },
                        onNavigateToOpenSourceLicenses = {
                            backStack.add(Destination.OpenSourceLicenses)
                        },
                        onNavigateToSessionHistory = { startDate, endDate ->
                            backStack.add(Destination.SessionHistory(startDate, endDate))
                        },
                        onNavigateToAppBlockHistory = { startDate, endDate ->
                            backStack.add(Destination.AppBlockHistory(startDate, endDate))
                        },
                        onNavigateToSessionDetails = { sessionId ->
                            backStack.add(Destination.SessionDetails(sessionId))
                        }
                    )
                }

                is Destination.OpenSourceLicenses -> NavEntry(key){
                    OpenSourceLicensesScreen(
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                is Destination.SessionHistory -> NavEntry(key) {
                    SessionHistoryScreen(
                        startDate = key.startDate,
                        endDate = key.endDate,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToSessionDetails = { sessionId ->
                            backStack.add(Destination.SessionDetails(sessionId))
                        }
                    )
                }

                is Destination.AppBlockHistory -> NavEntry(key) {
                    AppBlockHistoryScreen(
                        startDate = key.startDate,
                        endDate = key.endDate,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                is Destination.RoutineDetails -> NavEntry(key) {
                    RoutineDetailsScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
                        onRoutineSaved = {
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
