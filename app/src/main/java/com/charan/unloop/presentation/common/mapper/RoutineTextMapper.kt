package com.charan.unloop.presentation.common.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.charan.unloop.R
import com.charan.unloop.core.utils.DateTimeUtils.toDisplayString
import com.charan.unloop.core.utils.DateTimeUtils.toLocale
import com.charan.unloop.presentation.common.model.RoutineUiModel
import kotlinx.datetime.DayOfWeek
import java.time.format.TextStyle

@Composable
fun RoutineUiModel.toFormattedTime(): String = stringResource(
    R.string.routine_list_time_range,
    startTime.toDisplayString(),
    endTime.toDisplayString(),
)

@Composable
fun RoutineUiModel.toFormattedDays(): String {
    return when {
        selectedDays.isEmpty() -> stringResource(R.string.routine_no_days)
        selectedDays.size == 7 -> stringResource(R.string.routine_every_day)
        selectedDays.size == 5 && selectedDays.containsAll(
            listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        ) -> stringResource(R.string.routine_weekdays)
        selectedDays.size == 2 && selectedDays.containsAll(
            listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        ) -> stringResource(R.string.routine_weekends)
        else -> selectedDays.joinToString(stringResource(R.string.list_separator)) {
            it.toLocale(TextStyle.SHORT_STANDALONE)
        }
    }
}
