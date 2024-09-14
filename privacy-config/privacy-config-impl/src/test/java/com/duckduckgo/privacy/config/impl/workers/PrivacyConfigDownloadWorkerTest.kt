

package com.duckduckgo.privacy.config.impl.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.privacy.config.impl.PrivacyConfigDownloader
import com.duckduckgo.privacy.config.impl.PrivacyConfigDownloader.ConfigDownloadResult.Error
import com.duckduckgo.privacy.config.impl.PrivacyConfigDownloader.ConfigDownloadResult.Success
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PrivacyConfigDownloadWorkerTest {

    @get:Rule var coroutineRule = CoroutineTestRule()

    lateinit var testee: PrivacyConfigDownloadWorker
    private val mockPrivacyConfigDownloader: PrivacyConfigDownloader = mock()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mock()
    }

    @Test
    fun whenDoWorkIfDownloadReturnsTrueThenReturnSuccess() =
        runTest {
            whenever(mockPrivacyConfigDownloader.download()).thenReturn(Success)

            val worker =
                TestListenableWorkerBuilder<PrivacyConfigDownloadWorker>(context = context).build()

            worker.privacyConfigDownloader = mockPrivacyConfigDownloader
            worker.dispatcherProvider = coroutineRule.testDispatcherProvider

            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
        }

    @Test
    fun whenDoWorkIfDownloadReturnsFalseThenReturnRetry() =
        runTest {
            whenever(mockPrivacyConfigDownloader.download()).thenReturn(Error("error"))

            val worker =
                TestListenableWorkerBuilder<PrivacyConfigDownloadWorker>(context = context).build()

            worker.privacyConfigDownloader = mockPrivacyConfigDownloader
            worker.dispatcherProvider = coroutineRule.testDispatcherProvider

            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.retry()))
        }
}
