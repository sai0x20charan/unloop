package com.charan.unloop.data.model

import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.model.SessionWithSessionEvents
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    val routines : List<RoutineEntity>,
    val sessions : List<SessionWithSessionEvents>,
    @SerialName("metaData")
    val metadata : BackupMetadata
)
@Serializable
data class BackupMetadata(
    val appVersion : String,
    val appVersionCode : Int,
    val createdAt : LocalDateTime,
)
