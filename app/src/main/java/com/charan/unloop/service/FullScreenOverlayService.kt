package com.charan.unloop.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.charan.unloop.presentation.overlay.OverLayScreen
import com.charan.unloop.presentation.routine.UnlockChallengeType
import com.charan.unloop.ui.theme.UnloopTheme
import kotlinx.datetime.LocalTime

class FullScreenOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    companion object {
        const val EXTRA_UNLOCK_TYPE = "unlock_type"
        const val EXTRA_ROUTINE_ID = "routine_id"
        const val EXTRA_ROUTINE_NAME = "routine_name"
        const val EXTRA_APP_PACKAGE = "app_package"
        const val EXTRA_WAIT_SECONDS = "wait_seconds"
        const val EXTRA_END_TIME_SECOND_OF_DAY = "end_time_second_of_day"
    }

    private data class OverlayDetails(
        val routineId: String?,
        val routineName: String,
        val appPackage: String?,
        val unlockType: UnlockChallengeType,
        val waitSeconds: Int,
        val endTime: LocalTime,
    )

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var composeView: ComposeView? = null
    private var windowManager: WindowManager? = null
    private var details by mutableStateOf<OverlayDetails?>(null)
    // The monitor stops this service when the user leaves the blocked app.
    private var unlockedForCurrentVisit = false

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || windowManager == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        val unlockTypeName = intent.getStringExtra(EXTRA_UNLOCK_TYPE)
        val endSecondOfDay = intent.getIntExtra(EXTRA_END_TIME_SECOND_OF_DAY, -1)
        val endTime = if (endSecondOfDay in 0..86399) {
            LocalTime.fromSecondOfDay(endSecondOfDay)
        } else {
            LocalTime(23, 59)
        }
        val nextDetails = OverlayDetails(
            routineId = intent.getStringExtra(EXTRA_ROUTINE_ID),
            routineName = intent.getStringExtra(EXTRA_ROUTINE_NAME).orEmpty().ifBlank { "Focus time" },
            appPackage = intent.getStringExtra(EXTRA_APP_PACKAGE),
            unlockType = UnlockChallengeType.entries.firstOrNull { it.name == unlockTypeName }
                ?: UnlockChallengeType.WAIT_TIME,
            waitSeconds = intent.getIntExtra(EXTRA_WAIT_SECONDS, 30).coerceIn(5, 120),
            endTime = endTime
        )
        if (details != nextDetails) {
            unlockedForCurrentVisit = false
            details = nextDetails
        }
        Log.d("TAG", "onStartCommand: details=$details, unlockedForCurrentVisit=$unlockedForCurrentVisit")
        if (!unlockedForCurrentVisit) showOverlay()
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        if (!Settings.canDrawOverlays(this)) {
            Log.e("FullScreenOverlayService", "Overlay permission not granted!")
            stopSelf()
            return
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private fun showOverlay() {
        if (composeView != null) return
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FullScreenOverlayService)
            setViewTreeViewModelStoreOwner(this@FullScreenOverlayService)
            setViewTreeSavedStateRegistryOwner(this@FullScreenOverlayService)
            setContent {
                UnloopTheme {
                    details?.let { current ->
                        key(current) {
                            OverLayScreen(
                                unlockType = current.unlockType,
                                routineName = current.routineName,
                                appName = appLabel(current.appPackage),
                                waitTimeSeconds = current.waitSeconds,
                                endTime = current.endTime,
                                onKeepFocusing = ::returnHome,
                                onUnlock = {
                                    if (details?.unlockType == UnlockChallengeType.WAIT_TIME) {
                                        unlockedForCurrentVisit = true
                                        removeOverlay()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        composeView = view
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        try {
            windowManager?.addView(view, params)
        } catch (e: Exception) {
            Log.e("FullScreenOverlayService", "Failed to add overlay window", e)
            stopSelf()
        }
    }

    @Suppress("DEPRECATION")
    private fun appLabel(appPackage: String?): String {
        if (appPackage == null) return "This app"
        return try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(appPackage, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                packageManager.getApplicationInfo(appPackage, 0)
            }
            packageManager.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            "This app"
        }
    }

    private fun returnHome() {
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        stopSelf()
    }

    private fun removeOverlay() {
        composeView?.let { view ->
            if (view.isAttachedToWindow) {
                try {
                    windowManager?.removeView(view)
                } catch (e: IllegalArgumentException) {
                    Log.w("FullScreenOverlayService", "Overlay was already removed", e)
                }
            }
            view.disposeComposition()
        }
        composeView = null
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        removeOverlay()
        viewModelStore.clear()
        super.onDestroy()
    }
}
