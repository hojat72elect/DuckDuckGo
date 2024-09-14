

package com.duckduckgo.adclick.impl

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.duckduckgo.adclick.api.AdClickManager
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DataRemovalAdClickWorkerTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val mockAdClickManager: AdClickManager = mock()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = mock()
    }

    @Test
    fun whenDoWorkThenCallClearAllExpiredAsyncAndReturnSuccess() =
        runTest {
            val worker = TestListenableWorkerBuilder<DataRemovalAdClickWorker>(context = context).build()
            worker.adClickManager = mockAdClickManager
            worker.dispatchers = coroutineRule.testDispatcherProvider

            val result = worker.doWork()

            verify(mockAdClickManager).clearAllExpiredAsync()
            assertEquals(result, ListenableWorker.Result.success())
        }
}
