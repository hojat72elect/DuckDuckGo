

package com.duckduckgo.adclick.impl.pixels

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class AdClickDailyReportingWorkerTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val mockAdClickPixels: AdClickPixels = mock()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = mock()
    }

    @Test
    fun whenDoWorkThenCallFireCountPixelWithCorrectParamNameAndReturnSuccess() =
        runTest {
            val worker = TestListenableWorkerBuilder<AdClickDailyReportingWorker>(context = context).build()
            worker.adClickPixels = mockAdClickPixels
            worker.dispatchers = coroutineRule.testDispatcherProvider

            val result = worker.doWork()

            verify(mockAdClickPixels).fireCountPixel(AdClickPixelName.AD_CLICK_PAGELOADS_WITH_AD_ATTRIBUTION)
            Assert.assertEquals(result, ListenableWorker.Result.success())
        }
}
