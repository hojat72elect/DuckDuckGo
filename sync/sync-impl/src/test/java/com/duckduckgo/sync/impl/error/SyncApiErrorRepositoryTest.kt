package com.duckduckgo.sync.impl.error

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.sync.api.engine.SyncableType
import com.duckduckgo.sync.store.SyncDatabase
import com.duckduckgo.sync.store.model.SyncApiErrorType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncApiErrorRepositoryTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val db = Room.inMemoryDatabaseBuilder(context, SyncDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    private val dao = db.syncApiErrorsDao()

    private val testee = RealSyncApiErrorRepository(dao)

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenApiErrorAddedAndNotPresentThenNewEntryAdded() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncApiErrorType.OBJECT_LIMIT_EXCEEDED
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(feature, errorType)

        val error = dao.featureErrorByDate(feature.field, errorType.name, date)

        Assert.assertTrue(error != null)
        Assert.assertTrue(error!!.count == 1)
    }

    @Test
    fun whenApiErrorAddedAndPresentThenCountUpdated() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncApiErrorType.OBJECT_LIMIT_EXCEEDED
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(feature, errorType)
        testee.addError(feature, errorType)

        val error = dao.featureErrorByDate(feature.field, errorType.name, date)

        Assert.assertTrue(error != null)
        Assert.assertTrue(error!!.count == 2)
    }

    @Test
    fun whenNoErrorsStoredThenGettingErrorsReturnsEmpty() {
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()

        val errors = testee.getErrorsByDate(date)

        Assert.assertTrue(errors.isEmpty())
    }

    @Test
    fun whenNoErrorsStoredFromYesterdayThenGettingErrorsFromYesterdayReturnsEmpty() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncApiErrorType.OBJECT_LIMIT_EXCEEDED

        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

        testee.addError(feature, errorType)

        val errors = testee.getErrorsByDate(yesterday)

        Assert.assertTrue(errors.isEmpty())
    }

    @Test
    fun whenErrorsStoredThenGettingErrorsReturnsData() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncApiErrorType.OBJECT_LIMIT_EXCEEDED

        val today = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(feature, errorType)

        val errors = testee.getErrorsByDate(today)
        Assert.assertTrue(errors.isNotEmpty())

        val error = errors.first()
        Assert.assertTrue(error.name == "bookmarks_object_limit_exceeded_count")
        Assert.assertTrue(error.count == "1")
    }
}
