package com.charan.unloop.core.overlay

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppUsageChecker @Inject constructor(
    @ApplicationContext private val context: android.content.Context
) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getForegroundApp(): String? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5000

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        return generateSequence {
            if (events.hasNextEvent()) {
                events.getNextEvent(event)
                event
            } else null
        }
            .filter { it.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND }
            .map { it.packageName }
            .lastOrNull()
    }

}