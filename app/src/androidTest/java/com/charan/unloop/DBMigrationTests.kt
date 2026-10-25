package com.charan.unloop

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.charan.unloop.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNull
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
        // No version upgrade exists yet. Version 2 is the first exported schema.
        // Keep this starting version when adding future migrations.
        helper.createDatabase(BASELINE_VERSION).close()

        // Querying forces Room to open and validate the database at the current version.
        val db = openDatabase()
        try {
            assertNull(db.sessionDao().getSessionById("missing-session"))
        } finally {
            db.close()
        }
    }

    private fun openDatabase(): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        TEST_DB
    )
        // Register future manual migrations here with addMigrations(...), as in DatabaseModule.
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private companion object {
        const val BASELINE_VERSION = 2
        const val TEST_DB = "unloop-migration-test"
    }
}
