package com.charan.unloop.core.overlay

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppUsageChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var lastQueryTime = System.currentTimeMillis() - 10_000
    private var currentForeground: String? = null

    fun getForegroundApp(): String? {
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(lastQueryTime, now)
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (event.packageName != context.packageName) {
                        currentForeground = event.packageName
                    }
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (event.packageName == currentForeground) {
                        currentForeground = null
                    }
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> currentForeground = null
            }
        }
        lastQueryTime = now
        return currentForeground
    }
}

