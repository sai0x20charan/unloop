package com.charan.unloop.data.repository.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.charan.unloop.BuildConfig
import com.charan.unloop.core.utils.DateTimeUtils
import com.charan.unloop.core.utils.generateUuid
import com.charan.unloop.data.local.AppDatabase
import com.charan.unloop.data.local.entity.SessionEventEntity
import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.model.Backup
import com.charan.unloop.data.model.BackupMetadata
import com.charan.unloop.data.repository.BackupRepository
import com.charan.unloop.data.repository.RoutineRepository
import com.charan.unloop.data.repository.SessionEventRepository
import com.charan.unloop.data.repository.SessionRepository
import com.charan.unloop.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class BackupRepositoryImpl @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val sessionRepository: SessionRepository,
    private val sessionEventRepository: SessionEventRepository,
    private val database : AppDatabase,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context : Context
) : BackupRepository{

    companion object{
        const val BACKUP_MIME_TYPE = "application/json"


    }
    override suspend fun exportBackup(destinationUri: Uri): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val (routines, sessionsWithEvents) = database.useWriterConnection { connection ->
                    connection.immediateTransaction {
                        val routines = routineRepository.getAllRoutines()
                        val sessions = sessionRepository.getAllSessionsWithSessionEvents()
                        routines to sessions
                    }
                }

                val sessions = sessionsWithEvents.map {
                    if(it.sessionEntity.status == SessionStatus.RUNNING || it.sessionEntity.status == SessionStatus.PAUSED){
                        it.copy(
                            sessionEntity = it.sessionEntity.copy(
                                actualEndTime = DateTimeUtils.getCurrentDateTime(),
                                status = SessionStatus.COMPLETED
                            ),
                            sessionEvents = it.sessionEvents + SessionEventEntity(
                                sessionId = it.sessionEntity.id,
                                eventType = SessionEventType.COMPLETE_SESSION,
                                id = generateUuid(),
                                eventTime = DateTimeUtils.getCurrentDateTime()
                            )
                        )
                    } else{
                        it
                    }
                }

                val backup = Backup(
                    routines = routines,
                    sessions = sessions,
                    metadata = BackupMetadata(
                        appVersion = BuildConfig.VERSION_NAME,
                        appVersionCode = BuildConfig.VERSION_CODE,
                        createdAt = DateTimeUtils.getCurrentDateTimeUtc()
                    )
                )

                val backupJson = Json.encodeToString(
                    Backup.serializer(),
                    backup
                )

                val outputStream = context.contentResolver.openOutputStream(destinationUri)
                    ?: error("Unable to open backup destination")

                outputStream.bufferedWriter().use { writer ->
                    writer.write(backupJson)
                }

                Result.success(true)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                exception.printStackTrace()
                Result.failure(exception)
            }
        }


    override suspend fun importBackup(sourceUri: Uri): Result<Boolean> {
        return withContext(Dispatchers.IO){
            try {


                val inputStream = context.contentResolver.openInputStream(sourceUri)
                    ?: error("Unable to open backup file")

                inputStream.bufferedReader().use { reader ->
                    val backupJson = reader.readText()

                    val backup = Json.decodeFromString(
                        Backup.serializer(),
                        backupJson
                    )
                    var sessionsToImport = backup.sessions
                    val currentActiveSession = sessionRepository.getActiveSessionOnce()
                    if(currentActiveSession !=null){
                        if(sessionsToImport.any { it.sessionEntity.id == currentActiveSession.id }){
                            sessionsToImport = sessionsToImport.filter { it.sessionEntity.id != currentActiveSession.id }
                        }

                    }

                    database.useWriterConnection { connection ->
                        connection.immediateTransaction {
                            routineRepository.upsertRoutine(backup.routines)
                            if (sessionsToImport.isNotEmpty()) {
                                sessionRepository.insertSession(sessionsToImport.map { it.sessionEntity })
                                sessionEventRepository.deleteEventsForSessions(
                                    sessionsToImport.map { it.sessionEntity.id }
                                )
                                sessionEventRepository.insertSessionEvents(
                                    sessionsToImport.flatMap { it.sessionEvents }
                                )
                            }
                        }
                    }

                    backup.routines.forEach { routineEntity ->
                        sessionManager.cancelScheduledRoutine(routineEntity.id)
                        if(routineEntity.isEnabled && !routineEntity.isDeleted) {

                            sessionManager.scheduleNextRoutineStart(routineEntity.id)
                        }

                    }

                }
                inputStream.close()

                Result.success(true)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                exception.printStackTrace()
                Result.failure(exception)
            }
        }
    }

    override val suggestedBackupFileName: String
        get() {
            val appName = context.applicationInfo.loadLabel(context.packageManager)
            return "${appName}_Backup_${DateTimeUtils.getCurrentDateTime()}.json"
        }
}
