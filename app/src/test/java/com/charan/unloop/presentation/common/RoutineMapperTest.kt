package com.charan.unloop.presentation.common

import com.charan.unloop.presentation.common.mapper.toRoutineEntity
import com.charan.unloop.presentation.common.mapper.toRoutineUiModel
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.presentation.common.models.RoutineUiModel
import com.charan.unloop.presentation.routine.RoutineState
import com.charan.unloop.presentation.routine.UnlockChallengeType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class RoutineMapperTest {
    private val createdAt = LocalDateTime(2026, 10, 16, 8, 0)

    @Test
    fun roundTripPreservesRoutineData() {
        val entity = RoutineEntity(
            id = "saved-routine",
            name = "Deep work",
            description = "Finish the project",
            isAllDay = false,
            startTime = LocalTime(9, 15),
            endTime = LocalTime(14, 45),
            daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            unlockChallengeType = UnlockChallengeType.STRICT_MODE,
            waitDurationSeconds = 45,
            createdAt = createdAt,
            updatedAt = createdAt,
            isEnabled = false,
            isIntervalBlocking = false,
            intervalMinutes = 0,
            disableAppPackages = listOf("com.example.social", "com.example.video")
        )

        val restored = entity.toRoutineUiModel().toRoutineEntity(createdAt = entity.createdAt)

        assertEquals(entity.copy(updatedAt = restored.updatedAt), restored)
    }

    @Test
    fun creatingFromTheDraftKeepsTheExistingDefaults() {
        val entity = RoutineState().routine.toRoutineEntity()

        assertEquals(LocalTime(0, 0), entity.startTime)
        assertEquals(LocalTime(0, 0), entity.endTime)
        assertEquals(30, entity.waitDurationSeconds)
        assertEquals(entity.id, UUID.fromString(entity.id).toString())
    }

    @Test
    fun explicitIdentityAndCreationDateAreRespected() {
        val model = RoutineUiModel(id = "previous-id", name = "Updated routine")

        val entity = model.toRoutineEntity(id = "chosen-id", createdAt = createdAt)

        assertEquals("chosen-id", entity.id)
        assertEquals(createdAt, entity.createdAt)
    }
}
