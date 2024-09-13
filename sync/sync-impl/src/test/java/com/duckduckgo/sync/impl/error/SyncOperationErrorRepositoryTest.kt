package com.duckduckgo.sync.impl.error

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.sync.api.engine.SyncableType
import com.duckduckgo.sync.impl.pixels.SyncPixelParameters
import com.duckduckgo.sync.impl.pixels.SyncPixelParameters.DATA_PERSISTER_ERROR_PARAM
import com.duckduckgo.sync.impl.pixels.SyncPixelParameters.DATA_PROVIDER_ERROR_PARAM
import com.duckduckgo.sync.impl.pixels.SyncPixelParameters.ORPHANS_PRESENT
import com.duckduckgo.sync.impl.pixels.SyncPixelParameters.TIMESTAMP_CONFLICT
import com.duckduckgo.sync.store.SyncDatabase
import com.duckduckgo.sync.store.model.GENERIC_FEATURE
import com.duckduckgo.sync.store.model.SyncOperationErrorType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncOperationErrorRepositoryTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val db = Room.inMemoryDatabaseBuilder(context, SyncDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    private val dao = db.syncOperationErrorsDao()

    private val testee = RealSyncOperationErrorRepository(dao)

    @After
    fun after() {
        db.close()
    }

    @Test
    fun whenOperationErrorAddedAndNotPresentThenNewEntryAdded() {
        val errorType = SyncOperationErrorType.DATA_ENCRYPT
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(errorType)

        val error = dao.featureErrorByDate(GENERIC_FEATURE, errorType.name, date)

        Assert.assertTrue(error != null)
        Assert.assertTrue(error!!.count == 1)
    }

    @Test
    fun whenFeatureErrorAddedAndNotPresentThenNewEntryAdded() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncOperationErrorType.DATA_PERSISTER_ERROR
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(feature.field, errorType)

        val error = dao.featureErrorByDate(feature.field, errorType.name, date)

        Assert.assertTrue(error != null)
        Assert.assertTrue(error!!.count == 1)
    }

    @Test
    fun whenOperationErrorAddedAndPresentThenCountUpdated() {
        val errorType = SyncOperationErrorType.DATA_ENCRYPT
        val date = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(errorType)
        testee.addError(errorType)

        val error = dao.featureErrorByDate(GENERIC_FEATURE, errorType.name, date)

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
        val errorType = SyncOperationErrorType.DATA_ENCRYPT
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

        testee.addError(errorType)

        val errors = testee.getErrorsByDate(yesterday)

        Assert.assertTrue(errors.isEmpty())
    }

    @Test
    fun whenErrorsStoredThenGettingErrorsReturnsData() {
        val errorType = SyncOperationErrorType.DATA_ENCRYPT
        val today = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(errorType)

        val errors = testee.getErrorsByDate(today)
        Assert.assertTrue(errors.isNotEmpty())

        val error = errors.first()
        Assert.assertTrue(error.name == SyncPixelParameters.DATA_ENCRYPT_ERROR)
        Assert.assertTrue(error.count == "1")
    }

    @Test
    fun whenFeatureErrorsStoredThenGettingErrorsReturnsData() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncOperationErrorType.DATA_PERSISTER_ERROR
        val today = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(feature.field, errorType)

        val errors = testee.getErrorsByDate(today)
        Assert.assertTrue(errors.isNotEmpty())

        val error = errors.first()
        val expectedErrorName = String.format(Locale.US, DATA_PERSISTER_ERROR_PARAM, feature.field)
        Assert.assertTrue(error.name == expectedErrorName)
        Assert.assertTrue(error.count == "1")
    }

    @Test
    fun whenProviderErrorsStoredThenGettingErrorsReturnsData() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncOperationErrorType.DATA_PROVIDER_ERROR
        val today = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(feature.field, errorType)

        val errors = testee.getErrorsByDate(today)
        Assert.assertTrue(errors.isNotEmpty())

        val error = errors.first()
        val expectedErrorName = String.format(Locale.US, DATA_PROVIDER_ERROR_PARAM, feature.field)
        Assert.assertTrue(error.name == expectedErrorName)
        Assert.assertTrue(error.count == "1")
    }

    @Test
    fun whenTimestampErrorsStoredThenGettingErrorsReturnsData() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncOperationErrorType.TIMESTAMP_CONFLICT
        val today = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(feature.field, errorType)

        val errors = testee.getErrorsByDate(today)
        Assert.assertTrue(errors.isNotEmpty())

        val error = errors.first()
        val expectedErrorName = String.format(Locale.US, TIMESTAMP_CONFLICT, feature.field)
        Assert.assertTrue(error.name == expectedErrorName)
        Assert.assertTrue(error.count == "1")
    }

    @Test
    fun whenOrphansErrorsStoredThenGettingErrorsReturnsData() {
        val feature = SyncableType.BOOKMARKS
        val errorType = SyncOperationErrorType.ORPHANS_PRESENT
        val today = DatabaseDateFormatter.getUtcIsoLocalDate()

        testee.addError(feature.field, errorType)

        val errors = testee.getErrorsByDate(today)
        Assert.assertTrue(errors.isNotEmpty())

        val error = errors.first()
        val expectedErrorName = String.format(Locale.US, ORPHANS_PRESENT, feature.field)
        Assert.assertTrue(error.name == expectedErrorName)
        Assert.assertTrue(error.count == "1")
    }
}
