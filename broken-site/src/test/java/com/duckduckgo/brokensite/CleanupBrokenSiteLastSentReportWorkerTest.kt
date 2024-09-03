package com.duckduckgo.brokensite

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.duckduckgo.brokensite.impl.BrokenSiteReportRepository
import com.duckduckgo.brokensite.impl.CleanupBrokenSiteLastSentReportWorker
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class CleanupBrokenSiteLastSentReportWorkerTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val mockBrokenSiteReportRepository: BrokenSiteReportRepository = mock()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = mock()
    }

    @Test
    fun whenDoWorkThenCallCleanupOldEntriesAndReturnSuccess() =
        runTest {
            val worker =
                TestListenableWorkerBuilder<CleanupBrokenSiteLastSentReportWorker>(context = context).build()
            worker.brokenSiteReportRepository = mockBrokenSiteReportRepository
            worker.dispatchers = coroutineRule.testDispatcherProvider

            val result = worker.doWork()

            verify(mockBrokenSiteReportRepository).cleanupOldEntries()
            assertEquals(result, ListenableWorker.Result.success())
        }
}
