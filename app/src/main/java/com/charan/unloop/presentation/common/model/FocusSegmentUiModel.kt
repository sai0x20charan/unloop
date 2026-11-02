package com.charan.unloop.presentation.common.model

data class FocusSegmentUiModel(
    val startFraction: Float,
    val endFraction: Float,
    val isFocused: Boolean = true,
    val isEnded : Boolean = false
)
