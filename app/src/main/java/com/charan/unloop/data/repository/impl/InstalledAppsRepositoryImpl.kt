package com.charan.unloop.data.repository.impl

import com.charan.unloop.data.apps.InstalledAppsDataSource
import com.charan.unloop.data.model.InstalledApp
import com.charan.unloop.data.repository.InstalledAppsRepository
import javax.inject.Inject

class InstalledAppsRepositoryImpl @Inject constructor(
    private val installedAppsDataSource: InstalledAppsDataSource
) : InstalledAppsRepository {
    override suspend fun getInstalledApps(): List<InstalledApp> {
        return installedAppsDataSource.getInstalledApps()

    }

    override suspend fun getAppDetailsByPackageName(packageName: String) : InstalledApp {
        return installedAppsDataSource.getAppDetailsByPackage(packageName)

    }
}