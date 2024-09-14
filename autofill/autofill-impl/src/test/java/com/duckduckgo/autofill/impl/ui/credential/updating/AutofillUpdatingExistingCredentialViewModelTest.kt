

package com.duckduckgo.autofill.impl.ui.credential.updating

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutofillUpdatingExistingCredentialViewModelTest {

    private val testee = AutofillUpdatingExistingCredentialViewModel()

    @Test
    fun whenUsernameIsShortThenNoEllipsizing() {
        val result = testee.ellipsizeIfNecessary("foo")
        result.assertDoesNotEndInEllipsis()
        assertEquals("foo", result)
    }

    @Test
    fun whenUsernameIsExactlyOnLimitThenNoEllipsizing() {
        val usernameExactlyAsLongAsLimit = "A".repeat(50)
        val result = testee.ellipsizeIfNecessary(usernameExactlyAsLongAsLimit)
        result.assertDoesNotEndInEllipsis()
        assertEquals(usernameExactlyAsLongAsLimit, result)
    }

    @Test
    fun whenUsernameIsLongerThanLimitThenEllipsizing() {
        val usernameLongerThanLimit = "A".repeat(51)
        val result = testee.ellipsizeIfNecessary(usernameLongerThanLimit)
        result.assertEndsInEllipsis()
        assertEquals(50, result.length)
    }

    private fun String.assertEndsInEllipsis() {
        assertTrue(endsWith(Typography.ellipsis))
    }

    private fun String.assertDoesNotEndInEllipsis() {
        assertFalse(endsWith(Typography.ellipsis))
    }
}
