package com.charan.unloop

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.charan.unloop.data.local.AppDatabase
import com.charan.unloop.data.local.migration.MIGRATION_2_3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DBMigrationTests {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = context.getDatabasePath(TEST_DB),
        driver = BundledSQLiteDriver(),
        databaseClass = AppDatabase::class
    )

    @Before
    fun setUp() {
        context.deleteDatabase(TEST_DB)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrateAll() = runBlocking {
        helper.createDatabase(BASELINE_VERSION).close()

        // Querying forces Room to open and validate the database at the current version.
        val db = openDatabase()
        try {
            assertNull(db.sessionDao().getSessionById("missing-session"))
        } finally {
            db.close()
        }
    }

    @Test
    fun migrate2To3PreservesDataAndDefaultsToUnlockedControls() = runBlocking {
        helper.createDatabase(BASELINE_VERSION).apply {
            execSQL(
                """
                INSERT INTO routines (
                    id, name, isAllDay, startTime, endTime, daysOfWeek, unlockChallengeType,
                    waitDurationSeconds, createdAt, updatedAt, isEnabled, isDeleted,
                    isIntervalBlocking, disableAppPackages
                ) VALUES (
                    'saved-routine', 'Deep work', 0, 32400, 36000, 'MONDAY', 'WAIT_TIME',
                    30, 0, 0, 1, 0, 0, 'com.example.social'
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO sessions (
                    id, routineId, startTime, endTime, sessionStatus, sessionType,
                    blockedApps, unlockChallengeType, sessionName, waitDurationSeconds, createdAt
                ) VALUES (
                    'saved-session', 'saved-routine', 0, 3600000, 'RUNNING', 'SCHEDULED',
                    'com.example.social', 'WAIT_TIME', 'Deep work', 30, 0
                )
                """.trimIndent()
            )
            close()
        }
        helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).close()

        val db = openDatabase()
        try {
            val routine = db.routineDao().getRoutineById("saved-routine")
            val session = db.sessionDao().getSessionById("saved-session")
            assertNotNull(routine)
            assertNotNull(session)
            requireNotNull(routine)
            requireNotNull(session)
            assertEquals("Deep work", routine.name)
            assertEquals(listOf("com.example.social"), routine.disableAppPackages)
            assertEquals(30, routine.waitDurationSeconds)
            assertFalse(routine.areSessionControlsLocked)
            assertEquals(routine.id, session.routineId)
            assertEquals(listOf("com.example.social"), session.blockedApps)
            assertFalse(session.areSessionControlsLocked)

            db.routineDao().upsertRoutine(routine.copy(areSessionControlsLocked = true))
            db.sessionDao().updateSession(session.copy(areSessionControlsLocked = true))
            assertTrue(db.routineDao().getRoutineById(routine.id)!!.areSessionControlsLocked)
            assertTrue(db.sessionDao().getSessionById(session.id)!!.areSessionControlsLocked)
        } finally {
            db.close()
        }
    }

    private fun openDatabase(): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        TEST_DB
    )
        .addMigrations(MIGRATION_2_3)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private companion object {
        const val BASELINE_VERSION = 2
        const val TEST_DB = "unloop-migration-test"
    }
}
