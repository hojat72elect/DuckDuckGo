

package com.duckduckgo.autofill.impl.encoding

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UrlUnicodeNormalizerImplTest {

    private val testee = UrlUnicodeNormalizerImpl()

    @Test
    fun whenNormalizingToAsciiAndContainsNonAsciiThenOutputIdnaEncoded() {
        assertEquals("xn--7ca.com", testee.normalizeAscii("ç.com"))
    }

    @Test
    fun whenNormalizingToAsciiAndOnlyContainsAsciiThenThenInputAndOutputIdentical() {
        assertEquals("c.com", testee.normalizeAscii("c.com"))
    }

    @Test
    fun whenNormalizingToUnicodeAndContainsNonAsciiThenOutputContainsNonAscii() {
        assertEquals("ç.com", testee.normalizeUnicode("xn--7ca.com"))
    }

    @Test
    fun whenNormalizingToUnicodeAndOnlyContainsAsciiThenThenInputAndOutputIdentical() {
        assertEquals("c.com", testee.normalizeUnicode("c.com"))
    }
}
