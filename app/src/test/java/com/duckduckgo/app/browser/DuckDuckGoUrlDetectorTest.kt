

package com.duckduckgo.app.browser

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DuckDuckGoUrlDetectorTest {

    private lateinit var testee: DuckDuckGoUrlDetector

    @Before
    fun setup() {
        testee = DuckDuckGoUrlDetectorImpl()
    }

    @Test
    fun whenCheckingSearchTermThenIdentifiedAsNotDDGUrl() {
        assertFalse(testee.isDuckDuckGoUrl("foo"))
    }

    @Test
    fun whenCheckingNonDDGUrlThenIdentifiedAsNotDDGUrl() {
        assertFalse(testee.isDuckDuckGoUrl("example.com"))
    }

    @Test
    fun whenCheckingFullDDGUrlThenIdentifiedAsDDGUrl() {
        assertTrue(testee.isDuckDuckGoUrl("https://duckduckgo.com/?q=test%20search&tappv=android_0_2_0&t=ddg_android"))
    }

    @Test
    fun whenCheckingSubdomainAndETLDisDDGThenReturnTrue() {
        assertTrue(testee.isDuckDuckGoUrl("https://test.duckduckgo.com"))
    }

    @Test
    fun whenCheckingSubdomainAndETLDisNotDDGThenReturnFalse() {
        assertFalse(testee.isDuckDuckGoUrl("https://test.duckduckgo.test.com"))
    }

    @Test
    fun whenDDGUrlContainsQueryThenQueryCanBeExtracted() {
        val query = testee.extractQuery("https://duckduckgo.com?q=test%20search")
        assertEquals("test search", query)
    }

    @Test
    fun whenDDGUrlDoesNotContainsQueryThenQueryIsNull() {
        val query = testee.extractQuery("https://duckduckgo.com")
        assertNull(query)
    }

    @Test
    fun whenDDGUrlContainsQueryThenQueryDetected() {
        assertTrue(testee.isDuckDuckGoQueryUrl("https://duckduckgo.com?q=test%20search"))
    }

    @Test
    fun whenDDGUrlDoesNotContainsQueryThenQueryIsNotDetected() {
        assertFalse(testee.isDuckDuckGoQueryUrl("https://duckduckgo.com"))
    }

    @Test
    fun whenNonDDGUrlContainsQueryThenQueryIsNotDetected() {
        assertFalse(testee.isDuckDuckGoQueryUrl("https://example.com?q=test%20search"))
    }

    @Test
    fun whenDDGUrlContainsVerticalThenVerticalCanBeExtracted() {
        val vertical = testee.extractVertical("https://duckduckgo.com/?q=new+zealand+images&t=ffab&atb=v218-6&iar=images&iax=images&ia=images")
        assertEquals("images", vertical)
    }

    @Test
    fun whenDDGUrlDoesNotContainVerticalThenVerticalIsNull() {
        val vertical = testee.extractVertical("https://duckduckgo.com")
        assertNull(vertical)
    }

    @Test
    fun whenDDGUrlContainsVerticalThenVerticalUrlDetected() {
        assertTrue(testee.isDuckDuckGoVerticalUrl("https://duckduckgo.com?ia=images"))
    }

    @Test
    fun whenDDGUrlDoesNotContainsVerticalThenVerticalUrlIsNotDetected() {
        assertFalse(testee.isDuckDuckGoVerticalUrl("https://duckduckgo.com"))
    }

    @Test
    fun whenCheckingNonDDGUrThenVerticalUrlIsNotDetected() {
        assertFalse(testee.isDuckDuckGoVerticalUrl("https://example.com?ia=images"))
    }

    @Test
    fun whenDDGIsSettingsPageThenStaticPageIsDetected() {
        assertTrue(testee.isDuckDuckGoStaticUrl("https://duckduckgo.com/settings"))
    }

    @Test
    fun whenDDGIsParamsPageThenStaticPageIsDetected() {
        assertTrue(testee.isDuckDuckGoStaticUrl("https://duckduckgo.com/params"))
    }

    @Test
    fun whenDDGIsNotStaticPageThenStaticPageIsNotDetected() {
        assertFalse(testee.isDuckDuckGoStaticUrl("https://duckduckgo.com/something"))
    }

    @Test
    fun whenNonDDGThenStaticPageIsDetected() {
        assertFalse(testee.isDuckDuckGoStaticUrl("https://example.com/settings"))
    }

    @Test
    fun whenIsNotDuckDuckGoEmailUrlThenReturnFalse() {
        assertFalse(testee.isDuckDuckGoEmailUrl("https://example.com"))
    }

    @Test
    fun whenIsDuckDuckEmailUrlGoThenReturnTrue() {
        assertTrue(testee.isDuckDuckGoEmailUrl("https://duckduckgo.com/email"))
    }

    @Test
    fun whenUrlContainsSubdomainAndIsETLDForDuckDuckGoEmailUrlThenReturnTrue() {
        assertTrue(testee.isDuckDuckGoEmailUrl("https://test.duckduckgo.com/email"))
    }

    @Test
    fun whenUrlContainsSubdomainAndIsNotETLDForDuckDuckGoEmailUrlThenReturnTrue() {
        assertFalse(testee.isDuckDuckGoEmailUrl("https://test.duckduckgo.test.com/email"))
    }

    @Test
    fun whenUrlHasNoSchemeAndIsFromDuckDuckGoUrlThenReturnsFalse() {
        assertFalse(testee.isDuckDuckGoEmailUrl("duckduckgo.com/email"))
    }
}
