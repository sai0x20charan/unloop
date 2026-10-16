package com.charan.unloop.presentation.all_routines

import com.charan.unloop.presentation.common.models.RoutineUiModel

data class AllRoutinesState(
    val allRoutines: List<RoutineUiModel> = emptyList(),
)
