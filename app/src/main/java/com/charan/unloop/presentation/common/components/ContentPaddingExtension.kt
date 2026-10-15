package com.charan.unloop.presentation.common.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PaddingValues.toScreenContentPadding(
    start: Dp = 16.dp,
    top: Dp = 0.dp,
    end: Dp = 16.dp,
    bottom: Dp = 0.dp,
    includeStartInset: Boolean = true,
    includeTopInset: Boolean = true,
    includeEndInset: Boolean = true,
    includeBottomInset: Boolean = true,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = (if (includeStartInset) calculateStartPadding(layoutDirection) else 0.dp) + start,
        top = (if (includeTopInset) calculateTopPadding() else 0.dp) + top,
        end = (if (includeEndInset) calculateEndPadding(layoutDirection) else 0.dp) + end,
        bottom = (if (includeBottomInset) calculateBottomPadding() else 0.dp) + bottom,
    )
}