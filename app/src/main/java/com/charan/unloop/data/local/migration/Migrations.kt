package com.charan.unloop.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE routines ADD COLUMN areSessionControlsLocked INTEGER NOT NULL DEFAULT 0"
        )
        connection.execSQL(
            "ALTER TABLE sessions ADD COLUMN areSessionControlsLocked INTEGER NOT NULL DEFAULT 0"
        )
    }
}
