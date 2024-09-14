

package com.duckduckgo.autofill.sync

import android.content.Context
import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RealCredentialsSyncStoreTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val preferences = InMemorySharedPreferences()

    private val mockContext: Context = mock<Context>().apply {
        whenever(this.getSharedPreferences(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenReturn(preferences)
    }

    val testee = RealCredentialsSyncStore(mockContext, coroutineRule.testScope, coroutineRule.testDispatcherProvider)

    @Test
    fun whenNoValueIsSyncPausedThenReturnFalse() {
        assertFalse(testee.isSyncPaused)
    }

    @Test
    fun whenIsSyncPausedUpdatedThenEmitNewValue() = runTest {
        testee.isSyncPausedFlow().test {
            awaitItem()
            testee.isSyncPaused = true
            assertEquals(true, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
