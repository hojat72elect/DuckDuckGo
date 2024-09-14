

package com.duckduckgo.app.browser.autofill

import com.duckduckgo.autofill.impl.RealAutofillFireproofDialogSuppressor
import com.duckduckgo.autofill.impl.time.TimeProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RealAutofillFireproofDialogSuppressorTest {

    private val timeProvider: TimeProvider = mock()
    private val testee = RealAutofillFireproofDialogSuppressor(timeProvider)

    @Before
    fun before() {
        configureTimeNow()
    }

    @Test
    fun whenNoInteractionsThenNotPreventingPrompts() {
        assertFalse(testee.isAutofillPreventingFireproofPrompts())
    }

    @Test
    fun whenSaveOrUpdateDialogVisibleThenPreventingPrompts() {
        testee.autofillSaveOrUpdateDialogVisibilityChanged(true)
        assertTrue(testee.isAutofillPreventingFireproofPrompts())
    }

    @Test
    fun whenSaveOrUpdateDialogDismissedRecentlyThenPreventingPrompts() {
        testee.autofillSaveOrUpdateDialogVisibilityChanged(true)
        testee.autofillSaveOrUpdateDialogVisibilityChanged(false)
        assertTrue(testee.isAutofillPreventingFireproofPrompts())
    }

    @Test
    fun whenSaveOrUpdateDialogDismissedAWhileBackThenPreventingPrompts() {
        testee.autofillSaveOrUpdateDialogVisibilityChanged(true)
        testee.autofillSaveOrUpdateDialogVisibilityChanged(false)
        configureTimeNow(System.currentTimeMillis() + 20_000)
        assertFalse(testee.isAutofillPreventingFireproofPrompts())
    }

    private fun configureTimeNow(timeMillis: Long = System.currentTimeMillis()) {
        whenever(timeProvider.currentTimeMillis()).thenReturn(timeMillis)
    }
}
