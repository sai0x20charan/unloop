package com.charan.unloop.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.charan.unloop.data.local.dao.RoutineDao
import com.charan.unloop.data.local.dao.SessionDao
import com.charan.unloop.data.local.dao.SessionEventDao
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.entity.SessionEventEntity

@Database(
    entities = [RoutineEntity::class, SessionEntity::class, SessionEventEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao

    abstract fun sessionDao(): SessionDao

    abstract fun sessionEventDao(): SessionEventDao
    companion object {
        const val DATABASE_NAME = "unloop_database"
    }
}
