package com.charan.unloop.data.repository

import android.net.Uri

interface BackupRepository {
    suspend fun exportBackup(destinationUri : Uri) : Result<Boolean>

    suspend fun importBackup(sourceUri : Uri) : Result<Boolean>

    val suggestedBackupFileName : String
}