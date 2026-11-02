package com.charan.unloop.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.charan.unloop.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager = context.packageManager

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val launcherApps = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

        launcherApps
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo

                AppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = packageManager.getApplicationIcon(appInfo)
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    suspend fun getAppInfo(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val applicationInfo = packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )
            AppInfo(
                packageName = packageName,
                appName = packageManager.getApplicationLabel(applicationInfo).toString(),
                icon = packageManager.getApplicationIcon(applicationInfo)
            )
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
