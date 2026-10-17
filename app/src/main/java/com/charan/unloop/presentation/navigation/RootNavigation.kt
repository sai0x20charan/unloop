package com.charan.unloop.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.charan.unloop.presentation.routine.RoutineScreen

private val navigationAnimationSpec = tween<IntOffset>(
    durationMillis = 300,
    easing = LinearOutSlowInEasing
)

@Composable
fun RootNavigation() {
    val backStack = rememberNavBackStack(Destination.BottomBarNav)

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
                is Destination.BottomBarNav -> NavEntry(key) {
                    BottomBarNavigation(
                        onNavigateToCreateRoutine = {
                            backStack.add(Destination.Routine(it))
                        }
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
                else -> throw IllegalArgumentException("Unknown destination: $key")
            }
        }
    )
}
