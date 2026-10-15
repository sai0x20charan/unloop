package com.charan.unloop.data.repository

import com.charan.unloop.data.model.InstalledApp

interface InstalledAppsRepository {

    suspend fun getInstalledApps(): List<InstalledApp>
}