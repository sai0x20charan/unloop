package com.charan.unloop

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.charan.unloop.data.local.AppDatabase
import com.charan.unloop.data.local.Converters
import com.charan.unloop.data.local.entity.RoutineEntity
import com.charan.unloop.data.local.entity.SessionEntity
import com.charan.unloop.data.local.entity.SessionEventEntity
import com.charan.unloop.data.local.migration.MIGRATION_2_3
import com.charan.unloop.data.local.migration.MIGRATION_3_4
import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
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
            assertEquals(listOf("com.example.social"), routine.blockedAppPackages)
            assertEquals(30, routine.waitDurationSeconds)
            assertFalse(routine.areSessionControlsLocked)
            assertEquals(routine.id, session.routineId)
            assertEquals(listOf("com.example.social"), session.blockedAppPackages)
            assertFalse(session.areSessionControlsLocked)

            db.routineDao().upsertRoutine(routine.copy(areSessionControlsLocked = true))
            db.sessionDao().updateSession(session.copy(areSessionControlsLocked = true))
            assertTrue(db.routineDao().getRoutineById(routine.id)!!.areSessionControlsLocked)
            assertTrue(db.sessionDao().getSessionById(session.id)!!.areSessionControlsLocked)
        } finally {
            db.close()
        }
    }

    @Test
    fun migrate3To4PreservesRenamedColumnsAndSessionEvents() = runBlocking {
        helper.createDatabase(3).apply {
            execSQL(
                """
                INSERT INTO routines (
                    id, name, description, isAllDay, startTime, endTime, daysOfWeek,
                    unlockChallengeType, waitDurationSeconds, createdAt, updatedAt,
                    isEnabled, isDeleted, isIntervalBlocking, intervalMinutes,
                    disableAppPackages, areSessionControlsLocked
                ) VALUES (
                    'routine', 'Deep work', 'Morning focus', 0, 32400, 36000,
                    'MONDAY,WEDNESDAY', 'WAIT_TIME', 30, 1000, 2000, 1, 0, 1, 5,
                    'com.example.social,com.example.video', 1
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO sessions (
                    id, routineId, actualStartTime, actualEndTime, startTime, endTime,
                    sessionStatus, sessionType, blockedApps, unlockChallengeType,
                    sessionName, waitDurationSeconds, createdAt, pausedRemainingSeconds,
                    areSessionControlsLocked
                ) VALUES (
                    'session', 'routine', 3000, 3500000, 2000, 3600000, 'PAUSED', 'SCHEDULED',
                    'com.example.social,com.example.video', 'WAIT_TIME', 'Deep work', 30,
                    1000, 120, 1
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO sessions (
                    id, startTime, endTime, sessionStatus, sessionType, blockedApps,
                    unlockChallengeType, sessionName, createdAt, areSessionControlsLocked
                ) VALUES (
                    'instant', 4000, 7200000, 'RUNNING', 'INSTANT', NULL,
                    'WAIT_TIME', 'Quick focus', 4000, 0
                )
                """.trimIndent()
            )
            SessionEventType.entries.forEachIndexed { index, type ->
                execSQL(
                    """
                    INSERT INTO session_events (id, sessionId, eventTime, eventType, packageName)
                    VALUES ('event-$index', 'session', 3000, '${type.name}', 'com.example.social')
                    """.trimIndent()
                )
            }
            close()
        }
        helper.runMigrationsAndValidate(4, listOf(MIGRATION_3_4)).close()

        val db = openDatabase()
        try {
            val packages = listOf("com.example.social", "com.example.video")
            val expectedRoutine = RoutineEntity(
                id = "routine",
                name = "Deep work",
                description = "Morning focus",
                isAllDay = false,
                startTime = LocalTime(9, 0),
                endTime = LocalTime(10, 0),
                daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                unlockChallengeType = UnlockChallengeType.WAIT_TIME,
                waitDurationSeconds = 30,
                createdAt = Converters.longToLocalDateTime(1000),
                updatedAt = Converters.longToLocalDateTime(2000),
                isEnabled = true,
                isDeleted = false,
                isIntervalBlocking = true,
                intervalMinutes = 5,
                blockedAppPackages = packages,
                areSessionControlsLocked = true
            )
            val expectedSession = SessionEntity(
                id = "session",
                routineId = "routine",
                actualStartTime = Converters.longToLocalDateTime(3000),
                actualEndTime = Converters.longToLocalDateTime(3500000),
                scheduledStartTime = Converters.longToLocalDateTime(2000),
                scheduledEndTime = Converters.longToLocalDateTime(3600000),
                status = SessionStatus.PAUSED,
                type = SessionType.SCHEDULED,
                blockedAppPackages = packages,
                unlockChallengeType = UnlockChallengeType.WAIT_TIME,
                name = "Deep work",
                waitDurationSeconds = 30,
                createdAt = Converters.longToLocalDateTime(1000),
                pausedRemainingSeconds = 120,
                areSessionControlsLocked = true
            )
            val expectedInstant = SessionEntity(
                id = "instant",
                scheduledStartTime = Converters.longToLocalDateTime(4000),
                scheduledEndTime = Converters.longToLocalDateTime(7200000),
                status = SessionStatus.RUNNING,
                type = SessionType.INSTANT,
                unlockChallengeType = UnlockChallengeType.WAIT_TIME,
                name = "Quick focus",
                createdAt = Converters.longToLocalDateTime(4000)
            )
            val expectedEvents = SessionEventType.entries.mapIndexed { index, type ->
                SessionEventEntity(
                    id = "event-$index",
                    sessionId = "session",
                    eventTime = Converters.longToLocalDateTime(3000),
                    eventType = type,
                    packageName = "com.example.social"
                )
            }
            assertEquals(expectedRoutine, db.routineDao().getRoutineById("routine"))
            assertEquals(expectedSession, db.sessionDao().getSessionById("session"))
            assertEquals(expectedInstant, db.sessionDao().getLastInstantSession())
            assertEquals(
                expectedSession,
                db.sessionDao().getActiveSessionOnce(listOf(SessionStatus.PAUSED))
            )
            assertEquals(
                expectedInstant,
                db.sessionDao().getActiveSession(listOf(SessionStatus.RUNNING)).first()
            )
            val sessionWithEvents = db.sessionDao().getSessionWithSessionEventsById("session")
            assertEquals(expectedSession, sessionWithEvents.sessionEntity)
            assertEquals(expectedEvents.toSet(), sessionWithEvents.sessionEvents.toSet())

            db.sessionDao().updateSessionStatus("session", SessionStatus.COMPLETED, null)
            assertEquals(
                expectedSession.copy(status = SessionStatus.COMPLETED, actualEndTime = null),
                db.sessionDao().getSessionById("session")
            )
        } finally {
            db.close()
        }
    }

    private fun openDatabase(): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        TEST_DB
    )
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private companion object {
        const val BASELINE_VERSION = 2
        const val TEST_DB = "unloop-migration-test"
    }
}
