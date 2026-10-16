package com.charan.unloop.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.charan.unloop.presentation.all_routines.AllRoutinesScreen
import com.charan.unloop.presentation.routine.RoutineScreen

@Composable
fun RootNavigation() {
    val backStack = rememberNavBackStack(Destination.AllRoutines)
    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = { key ->
            when (key) {
                is Destination.AllRoutines -> NavEntry(key) {
                    AllRoutinesScreen(
                        onNavigateToCreateRoutine = {
                            backStack.add(Destination.Routine(it))
                        },

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
