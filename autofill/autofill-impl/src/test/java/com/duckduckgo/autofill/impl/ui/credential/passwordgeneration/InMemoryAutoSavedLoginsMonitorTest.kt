

package com.duckduckgo.autofill.impl.ui.credential.passwordgeneration

import org.junit.Assert.*
import org.junit.Test

class InMemoryAutoSavedLoginsMonitorTest {
    private val testee = InMemoryAutoSavedLoginsMonitor()

    @Test
    fun whenValueSetThenReturnedFromGetFunction() {
        val loginId: Long = 1
        val tabId = "abc"
        testee.setAutoSavedLoginId(loginId, tabId)
        assertEquals(loginId, testee.getAutoSavedLoginId(tabId))
    }

    @Test
    fun whenValueSetThenClearedThenNotReturnedFromGetFunction() {
        val loginId: Long = 1
        val tabId = "abc"
        testee.setAutoSavedLoginId(loginId, tabId)
        testee.clearAutoSavedLoginId(tabId)
        assertNull(testee.getAutoSavedLoginId(tabId))
    }
}
