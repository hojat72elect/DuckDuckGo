

package com.duckduckgo.autofill.impl.deviceauth

import com.duckduckgo.autofill.impl.time.TimeProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AutofillTimeBasedAuthorizationGracePeriodTest {

    private val timeProvider: TimeProvider = mock()
    private val testee = AutofillTimeBasedAuthorizationGracePeriod(timeProvider)

    @Test
    fun whenNoInteractionsThenAuthRequired() {
        assertTrue(testee.isAuthRequired())
    }

    @Test
    fun whenLastSuccessfulAuthWasBeforeGracePeriodThenAuthRequired() {
        recordAuthorizationInDistantPast()
        timeProvider.reset()
        assertTrue(testee.isAuthRequired())
    }

    @Test
    fun whenLastSuccessfulAuthWasWithinGracePeriodThenAuthNotRequired() {
        recordAuthorizationWithinGracePeriod()
        timeProvider.reset()
        assertFalse(testee.isAuthRequired())
    }

    @Test
    fun whenLastSuccessfulAuthWasWithinGracePeriodButInvalidatedThenAuthRequired() {
        recordAuthorizationWithinGracePeriod()
        timeProvider.reset()
        testee.invalidate()
        assertTrue(testee.isAuthRequired())
    }

    private fun recordAuthorizationInDistantPast() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(0)
        testee.recordSuccessfulAuthorization()
    }

    private fun recordAuthorizationWithinGracePeriod() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(System.currentTimeMillis())
        testee.recordSuccessfulAuthorization()
    }

    private fun TimeProvider.reset() = whenever(this.currentTimeMillis()).thenReturn(System.currentTimeMillis())
}
