

package com.duckduckgo.autofill.impl.ui.credential.management

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegexBasedUrlIdentifierTest {

    private val testee = RegexBasedUrlIdentifier()

    @Test
    fun whenUrlIsNullThenNotClassedAsUrl() {
        assertFalse(testee.isLikelyAUrl(null))
    }

    @Test
    fun whenUrlIsEmptyStringThenNotClassedAsUrl() {
        assertFalse(testee.isLikelyAUrl(""))
    }

    @Test
    fun whenUrlIsBlankStringThenNotClassedAsUrl() {
        assertFalse(testee.isLikelyAUrl("   "))
    }

    @Test
    fun whenUrlIsAnIpAddressThenIsClassedAsUrl() {
        assertTrue(testee.isLikelyAUrl("192.168.1.100"))
    }

    @Test
    fun whenUrlIsSimpleUrlThenIsClassedAsUrl() {
        assertTrue(testee.isLikelyAUrl("example.com"))
    }

    @Test
    fun whenUrlHasPortThenIsClassedAsUrl() {
        assertTrue(testee.isLikelyAUrl("example.com:1234"))
    }
}
