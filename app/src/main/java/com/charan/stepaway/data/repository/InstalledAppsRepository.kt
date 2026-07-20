package com.charan.stepaway.data.repository

import com.charan.stepaway.data.model.InstalledApp

interface InstalledAppsRepository {

    suspend fun getInstalledApps(): List<InstalledApp>
}