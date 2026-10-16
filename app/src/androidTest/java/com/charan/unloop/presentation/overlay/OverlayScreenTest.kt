package com.charan.unloop.presentation.overlay

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.charan.unloop.presentation.routine.UnlockChallengeType
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverlayScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private var unlocks = 0
    private val holdButton get() = compose.onNodeWithText("Hold for 5 seconds to unlock")

    @Test
    fun holdingForTheFullDurationUnlocksOnlyOnce() {
        showOverlay()
        press()
        compose.mainClock.advanceTimeBy(5_500)
        compose.runOnIdle { assertEquals(1, unlocks) }
        compose.mainClock.advanceTimeBy(5_500)
        compose.runOnIdle { assertEquals(1, unlocks) }
        release()
    }

    @Test
    fun releasingEarlyResetsTheRequiredHoldDuration() {
        showOverlay()
        press()
        compose.mainClock.advanceTimeBy(3_000)
        release()
        compose.mainClock.advanceTimeBy(6_000)
        compose.runOnIdle { assertEquals(0, unlocks) }

        press()
        compose.mainClock.advanceTimeBy(3_000)
        compose.runOnIdle { assertEquals(0, unlocks) }
        compose.mainClock.advanceTimeBy(3_000)
        compose.runOnIdle { assertEquals(1, unlocks) }
        release()
    }

    @Test
    fun tappingOrCancellingDoesNotUnlock() {
        showOverlay()
        holdButton.performTouchInput { click() }
        compose.mainClock.advanceTimeBy(6_000)
        compose.runOnIdle { assertEquals(0, unlocks) }

        press()
        compose.mainClock.advanceTimeBy(3_000)
        holdButton.performTouchInput { cancel() }
        compose.mainClock.advanceTimeBy(6_000)
        compose.runOnIdle { assertEquals(0, unlocks) }
    }

    @Test
    fun strictModeHasNoHoldToUnlockControl() {
        showOverlay(UnlockChallengeType.STRICT_MODE)
        holdButton.assertDoesNotExist()
        compose.mainClock.advanceTimeBy(6_000)
        compose.runOnIdle { assertEquals(0, unlocks) }
    }

    private fun showOverlay(type: UnlockChallengeType = UnlockChallengeType.WAIT_TIME) {
        compose.setContent {
            OverLayScreen(
                unlockType = type,
                routineName = "Morning focus",
                appName = "Instagram",
                endTime = LocalTime(10, 20),
                waitTimeSeconds = 5,
                onUnlock = { unlocks++ }
            )
        }
        compose.waitForIdle()
        if (type == UnlockChallengeType.WAIT_TIME) holdButton.performScrollTo()
        compose.mainClock.autoAdvance = false
    }

    private fun press() {
        holdButton.performTouchInput { down(center) }
        // Allow the button's delayed press interaction inside the scroll container to start.
        compose.mainClock.advanceTimeBy(250)
    }

    private fun release() {
        holdButton.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(250)
    }
}
