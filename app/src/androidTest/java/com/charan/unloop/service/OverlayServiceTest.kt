package com.charan.unloop.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.os.SystemClock
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.charan.unloop.MainActivity
import com.charan.unloop.presentation.routine.UnlockChallengeType
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class OverlayServiceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val automation = instrumentation.uiAutomation

    // Grant SYSTEM_ALERT_WINDOW through appops on the test emulator before running.
    @Test
    fun startedServiceReceivesDetailsAndUpdatesTheVisibleOverlay() {
        assumeTrue(Settings.canDrawOverlays(context))
        val previousInfo = automation.serviceInfo
        automation.serviceInfo = AccessibilityServiceInfo().apply {
            flags = previousInfo.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            eventTypes = previousInfo.eventTypes
            feedbackType = previousInfo.feedbackType
        }
        val serviceIntent = Intent(context, FullScreenOverlayService::class.java).apply {
            putExtra(FullScreenOverlayService.EXTRA_ROUTINE_ID, "overlay-test")
            putExtra(FullScreenOverlayService.EXTRA_ROUTINE_NAME, "Morning focus")
            putExtra(FullScreenOverlayService.EXTRA_APP_PACKAGE, "com.android.chrome")
            putExtra(FullScreenOverlayService.EXTRA_WAIT_SECONDS, 5)
            putExtra(FullScreenOverlayService.EXTRA_UNLOCK_TYPE, UnlockChallengeType.WAIT_TIME.name)
        }
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                context.startService(serviceIntent)
                awaitText("Morning focus")
                awaitText("Wait 5 seconds to open")
                captureIfRequested("wait-light.png")

                serviceIntent.putExtra(FullScreenOverlayService.EXTRA_UNLOCK_TYPE, UnlockChallengeType.STRICT_MODE.name)
                context.startService(serviceIntent)
                awaitText("No unlocks during this routine")
                assertTrue(!hasText("Wait 5 seconds to open"))
                captureIfRequested("strict-light.png")
            }
        } finally {
            context.stopService(serviceIntent)
            automation.serviceInfo = previousInfo
        }
    }

    private fun hasText(text: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) automation.clearCache()
        return automation.windows.any { window ->
            window.root?.let { root ->
                root.refresh()
                root.containsText(text)
            } == true
        }
    }

    private fun AccessibilityNodeInfo.containsText(expected: String): Boolean {
        if (text?.contains(expected) == true) return true
        return (0 until childCount).any { getChild(it)?.containsText(expected) == true }
    }

    private fun awaitText(text: String) {
        val deadline = SystemClock.elapsedRealtime() + 8_000
        while (!hasText(text) && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(100)
        assertTrue("Overlay should display '$text'", hasText(text))
    }

    private fun captureIfRequested(name: String) {
        if (InstrumentationRegistry.getArguments().getString("captureOverlayScreenshots") != "true") return
        instrumentation.waitForIdleSync()
        SystemClock.sleep(400)
        val directory = context.getExternalFilesDir("overlay-qa")!!
        directory.mkdirs()
        File(directory, name).outputStream().use {
            automation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
