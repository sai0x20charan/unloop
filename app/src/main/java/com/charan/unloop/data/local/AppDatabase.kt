package com.charan.unloop.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.charan.unloop.data.local.dao.RoutineDao
import com.charan.unloop.data.local.entity.RoutineEntity

@Database(
    entities = [RoutineEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    companion object {
        const val DATABASE_NAME = "step_away_database"
    }
}