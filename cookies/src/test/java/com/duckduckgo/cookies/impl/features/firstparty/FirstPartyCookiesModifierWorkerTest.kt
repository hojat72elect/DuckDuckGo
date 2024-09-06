package com.duckduckgo.cookies.impl.features.firstparty

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class FirstPartyCookiesModifierWorkerTest {
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val mockFirstPartyCookiesModifier: FirstPartyCookiesModifier = mock()
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mock()
    }

    @Test
    fun whenDoWorkThenReturnSuccess() = runTest {
        val worker =
            TestListenableWorkerBuilder<FirstPartyCookiesModifierWorker>(context = context).build()

        worker.firstPartyCookiesModifier = mockFirstPartyCookiesModifier
        worker.dispatcherProvider = coroutineRule.testDispatcherProvider

        val result = worker.doWork()
        assertThat(result, `is`(Result.success()))
    }
}
