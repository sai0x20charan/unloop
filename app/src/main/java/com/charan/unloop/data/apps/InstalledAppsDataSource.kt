package com.charan.unloop.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.charan.unloop.data.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InstalledAppsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager = context.packageManager

    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val launcherApps = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

        launcherApps
            .distinctBy { it.activityInfo.packageName  }
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo

                InstalledApp(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = packageManager.getApplicationIcon(appInfo)
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    suspend fun getAppDetailsByPackage(packageName : String) : InstalledApp{
        return InstalledApp(
            packageName = packageName,
            appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString(),
            icon = packageManager.getApplicationIcon(packageName)

        )
    }
}