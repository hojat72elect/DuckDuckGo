

package com.duckduckgo.savedsites.impl.sync

import android.content.Context
import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RealSavedSitesSyncStoreTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val preferences = InMemorySharedPreferences()

    private val mockContext: Context = mock<Context>().apply {
        whenever(this.getSharedPreferences(anyString(), anyInt())).thenReturn(preferences)
    }

    val testee = RealSavedSitesSyncStore(mockContext, coroutineRule.testScope, coroutineRule.testDispatcherProvider)

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
