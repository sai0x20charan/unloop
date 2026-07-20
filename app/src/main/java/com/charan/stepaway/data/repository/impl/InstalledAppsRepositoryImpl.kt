package com.charan.stepaway.data.repository.impl

import com.charan.stepaway.data.apps.InstalledAppsDataSource
import com.charan.stepaway.data.model.InstalledApp
import com.charan.stepaway.data.repository.InstalledAppsRepository
import javax.inject.Inject

class InstalledAppsRepositoryImpl @Inject constructor(
    private val installedAppsDataSource: InstalledAppsDataSource
) : InstalledAppsRepository {
    override suspend fun getInstalledApps(): List<InstalledApp> {
        return installedAppsDataSource.getInstalledApps()

    }
}