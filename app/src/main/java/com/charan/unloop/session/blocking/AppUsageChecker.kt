package com.charan.unloop.session.blocking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppUsageChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var lastQueryTime = System.currentTimeMillis() - 10_000
    private val resumedPackages = mutableSetOf<String?>()

    fun getForegroundApp(): String? {
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(lastQueryTime, now)
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    val packageName = event.packageName
                    resumedPackages.remove(packageName)
                    resumedPackages.add(packageName)
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    resumedPackages.remove(event.packageName)
                }
                // A deep link can stop an older instance after another instance resumes.
                UsageEvents.Event.ACTIVITY_STOPPED -> Unit
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> resumedPackages.clear()
            }
        }
        lastQueryTime = now
        return resumedPackages.lastOrNull()
    }
}

