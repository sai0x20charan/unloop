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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE routines RENAME COLUMN disableAppPackages TO blockedAppPackages")
        connection.execSQL("ALTER TABLE sessions RENAME COLUMN blockedApps TO blockedAppPackages")
        connection.execSQL("ALTER TABLE sessions RENAME COLUMN sessionName TO name")
        connection.execSQL("ALTER TABLE sessions RENAME COLUMN sessionStatus TO status")
        connection.execSQL("ALTER TABLE sessions RENAME COLUMN sessionType TO type")
        connection.execSQL("ALTER TABLE sessions RENAME COLUMN startTime TO scheduledStartTime")
        connection.execSQL("ALTER TABLE sessions RENAME COLUMN endTime TO scheduledEndTime")
    }
}
