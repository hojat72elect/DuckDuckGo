package com.duckduckgo.app.browser.httperrors

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class HttpErrorDailyReportingWorkerTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val mockHttpErrorPixels: HttpErrorPixels = mock()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = mock()
    }

    @Test
    fun whenDoWorkThenCallFireCountPixelWithCorrectParamNameAndReturnSuccess() =
        runTest {
            val worker =
                TestListenableWorkerBuilder<HttpErrorDailyReportingWorker>(context = context).build()
            worker.httpErrorPixels = mockHttpErrorPixels
            worker.dispatchers = coroutineRule.testDispatcherProvider

            val result = worker.doWork()

            verify(mockHttpErrorPixels).fireCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_400_DAILY)
            verify(mockHttpErrorPixels).fireCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_4XX_DAILY)
            verify(mockHttpErrorPixels).fireCountPixel(HttpErrorPixelName.WEBVIEW_RECEIVED_HTTP_ERROR_5XX_DAILY)
            assertEquals(result, ListenableWorker.Result.success())
        }
}
