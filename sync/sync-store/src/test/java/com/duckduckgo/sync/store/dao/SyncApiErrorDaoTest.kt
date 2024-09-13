package com.duckduckgo.sync.store.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.sync.store.SyncDatabase
import com.duckduckgo.sync.store.model.SyncApiError
import com.duckduckgo.sync.store.model.SyncApiErrorType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncApiErrorDaoTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: SyncDatabase
    private lateinit var dao: SyncApiErrorDao

    @Before
    fun before() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            SyncDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = db.syncApiErrorsDao()
    }

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenApiErrorAddedThenItCanBeRetrieved() = runTest {
        val feature = "bookmarks"
        val errorType = SyncApiErrorType.OBJECT_LIMIT_EXCEEDED
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()

        val insert = SyncApiError(feature = feature, errorType = errorType, count = 1, date = date)
        dao.insert(insert)

        val error = dao.featureErrorByDate(feature, errorType.name, date)
        Assert.assertTrue(error!!.count == 1)
    }

    @Test
    fun whenApiErrorIncrementedThenCounterIncremented() = runTest {
        val feature = "bookmarks"
        val errorType = SyncApiErrorType.OBJECT_LIMIT_EXCEEDED
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()

        val insert = SyncApiError(feature = feature, errorType = errorType, count = 1, date = date)
        dao.insert(insert)

        val error = dao.featureErrorByDate(feature, errorType.name, date)
        Assert.assertTrue(error!!.count == 1)

        dao.incrementCount(feature, errorType.name, date)

        val errorIncremented = dao.featureErrorByDate(feature, errorType.name, date)
        Assert.assertTrue(errorIncremented!!.count == 2)
    }
}
