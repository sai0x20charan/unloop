package com.charan.unloop.presentation.routines

import com.charan.unloop.presentation.common.model.RoutineUiModel

data class RoutinesState(
    val is24Hour: Boolean = false,
    val allRoutines: List<RoutineUiModel> = emptyList(),
)
