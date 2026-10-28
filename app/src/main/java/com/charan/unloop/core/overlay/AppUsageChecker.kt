package com.charan.unloop.core.overlay

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppUsageChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var lastQueryTime = System.currentTimeMillis() - 10_000
    private val visibleActivities = mutableSetOf<String?>()

    fun getForegroundApp(): String? {
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(lastQueryTime, now)
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    val activity = event.packageName
                    visibleActivities.remove(activity)
                    visibleActivities.add(activity)
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    // Older Android versions do not report ACTIVITY_STOPPED.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        visibleActivities.remove(event.packageName)
                    }
                }
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    visibleActivities.remove(event.packageName)
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> visibleActivities.clear()
            }
        }
        lastQueryTime = now
        return visibleActivities.lastOrNull()
    }
}

