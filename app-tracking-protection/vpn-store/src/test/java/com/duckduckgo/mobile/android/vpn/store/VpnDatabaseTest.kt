

package com.duckduckgo.mobile.android.vpn.store

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VpnDatabaseTest {
    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testHelper =
        MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), VpnDatabase::class.qualifiedName!!, FrameworkSQLiteOpenHelperFactory())

    @Test
    fun whenTestingAllMigrationsThenSucceeds() {
        createDatabase(18)

        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            VpnDatabase::class.java,
            TEST_DB_NAME,
        ).addMigrations(*VpnDatabase.ALL_MIGRATIONS.toTypedArray()).build().apply {
            openHelper.writableDatabase.close()
        }
    }

    private fun createDatabase(version: Int) {
        testHelper.createDatabase(TEST_DB_NAME, version).apply {
            close()
        }
    }

    companion object {
        private const val TEST_DB_NAME = "TEST_DB_NAME"
    }
}
