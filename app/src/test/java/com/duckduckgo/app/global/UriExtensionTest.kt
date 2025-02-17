

package com.duckduckgo.app.global

import android.net.Uri
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.common.utils.absoluteString
import com.duckduckgo.common.utils.baseHost
import com.duckduckgo.common.utils.domain
import com.duckduckgo.common.utils.extractDomain
import com.duckduckgo.common.utils.faviconLocation
import com.duckduckgo.common.utils.hasIpHost
import com.duckduckgo.common.utils.isHttp
import com.duckduckgo.common.utils.isHttps
import com.duckduckgo.common.utils.isHttpsVersionOfUri
import com.duckduckgo.common.utils.isMobileSite
import com.duckduckgo.common.utils.toDesktopUri
import com.duckduckgo.common.utils.toStringDropScheme
import com.duckduckgo.common.utils.withScheme
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UriExtensionTest {

    @Test
    fun whenUriDoesNotHaveASchemeThenWithSchemeAppendsHttp() {
        val url = "someurl"
        assertEquals("http://$url", Uri.parse(url).withScheme().toString())
    }

    @Test
    fun whenUriHasASchemeThenWithSchemeHasNoEffect() {
        val url = "http://someurl"
        assertEquals(url, Uri.parse(url).withScheme().toString())
    }

    @Test
    fun whenUriBeginsWithWwwThenBaseHostReturnsWithoutWww() {
        val url = "http://www.example.com"
        assertEquals("example.com", Uri.parse(url).baseHost)
    }

    @Test
    fun whenUriDoesNotBeginWithWwwThenBaseHosReturnsWithSameHost() {
        val url = "http://example.com"
        assertEquals("example.com", Uri.parse(url).baseHost)
    }

    @Test
    fun whenUriDoesNotHaveASchemeThenBaseHostStillResolvesHost() {
        val url = "www.example.com"
        assertEquals("example.com", Uri.parse(url).baseHost)
    }

    @Test
    fun whenUriContainsInvalidHostThenBaseHostIsNull() {
        val url = "about:blank"
        assertNull(Uri.parse(url).baseHost)
    }

    @Test
    fun whenUriIsHttpIrrespectiveOfCaseThenIsHttpIsTrue() {
        assertTrue(Uri.parse("http://example.com").isHttp)
        assertTrue(Uri.parse("HTTP://example.com").isHttp)
    }

    @Test
    fun whenUriIsHttpsThenIsHttpIsFalse() {
        assertFalse(Uri.parse("https://example.com").isHttp)
    }

    @Test
    fun whenUriIsMalformedThenIsHttpIsFalse() {
        assertFalse(Uri.parse("[example com]").isHttp)
    }

    @Test
    fun whenUriIsHttpsIrrespectiveOfCaseThenIsHttpsIsTrue() {
        assertTrue(Uri.parse("https://example.com").isHttps)
        assertTrue(Uri.parse("HTTPS://example.com").isHttps)
    }

    @Test
    fun whenUriIsHttpThenIsHttpsIsFalse() {
        assertFalse(Uri.parse("http://example.com").isHttps)
    }

    @Test
    fun whenUriIsHttpsAndOtherIsHttpButOtherwiseIdenticalThenIsHttpsVersionOfOtherIsTrue() {
        val uri = Uri.parse("https://example.com")
        val other = Uri.parse("http://example.com")
        assertTrue(uri.isHttpsVersionOfUri(other))
    }

    @Test
    fun whenUriIsHttpsAndOtherIsHttpButNotOtherwiseIdenticalThenIsHttpsVersionOfOtherIsFalse() {
        val uri = Uri.parse("https://example.com")
        val other = Uri.parse("http://example.com/path")
        assertFalse(uri.isHttpsVersionOfUri(other))
    }

    @Test
    fun whenUriIsHttpThenIsHttpsVersionOfOtherIsFalse() {
        val uri = Uri.parse("http://example.com")
        val other = Uri.parse("http://example.com")
        assertFalse(uri.isHttpsVersionOfUri(other))
    }

    @Test
    fun whenUriIsHttpsAndOtherIsHttpsThenIsHttpsVersionOfOtherIsFalse() {
        val uri = Uri.parse("https://example.com")
        val other = Uri.parse("https://example.com")
        assertFalse(uri.isHttpsVersionOfUri(other))
    }

    @Test
    fun whenUriIsMalformedThenIsHtpsIsFalse() {
        assertFalse(Uri.parse("[example com]").isHttps)
    }

    @Test
    fun whenIpUriThenHasIpHostIsTrue() {
        assertTrue(Uri.parse("https://54.229.105.203/something").hasIpHost)
        assertTrue(Uri.parse("54.229.105.203/something").hasIpHost)
    }

    @Test
    fun whenIpWithPortUriThenHasIpHostIsTrue() {
        assertTrue(Uri.parse("https://54.229.105.203:999/something").hasIpHost)
        assertTrue(Uri.parse("54.229.105.203:999/something").hasIpHost)
    }

    @Test
    fun whenIpWithPortUriThenPortNumberParsedSuccessfully() {
        assertEquals(999, Uri.parse("https://54.229.105.203:999/something").port)
    }

    @Test
    fun whenValidIpAddressWithPortParsedWithSchemeThenPortNumberParsedSuccessfully() {
        assertEquals(999, Uri.parse("121.33.2.11:999").withScheme().port)
    }

    @Test
    fun whenStandardUriThenHasIpHostIsFalse() {
        assertFalse(Uri.parse("http://example.com").hasIpHost)
    }

    @Test
    fun whenUrlStartsMDotThenIdentifiedAsMobileSite() {
        assertTrue(Uri.parse("https://m.example.com").isMobileSite)
    }

    @Test
    fun whenUrlStartsMobileDotThenIdentifiedAsMobileSite() {
        assertTrue(Uri.parse("https://mobile.example.com").isMobileSite)
    }

    @Test
    fun whenUrlSubdomainEndsWithMThenNotIdentifiedAsMobileSite() {
        assertFalse(Uri.parse("https://adam.example.com").isMobileSite)
    }

    @Test
    fun whenUrlDoesNotStartWithMDotThenNotIdentifiedAsMobileSite() {
        assertFalse(Uri.parse("https://example.com").isMobileSite)
    }

    @Test
    fun whenConvertingMobileSiteToDesktopSiteThenShortMobilePrefixStripped() {
        val converted = Uri.parse("https://m.example.com").toDesktopUri()
        assertEquals("https://example.com", converted.toString())
    }

    @Test
    fun whenConvertingMobileSiteToDesktopSiteThenLongMobilePrefixStripped() {
        val converted = Uri.parse("https://mobile.example.com").toDesktopUri()
        assertEquals("https://example.com", converted.toString())
    }

    @Test
    fun whenConvertingMobileSiteToDesktopSiteThenMultipleMobilePrefixesStripped() {
        val converted = Uri.parse("https://mobile.m.example.com").toDesktopUri()
        assertEquals("https://example.com", converted.toString())
    }

    @Test
    fun whenConvertingDesktopSiteToDesktopSiteThenUrlUnchanged() {
        val converted = Uri.parse("https://example.com").toDesktopUri()
        assertEquals("https://example.com", converted.toString())
    }

    @Test
    fun whenGettingAbsoluteStringThenDoNotReturnQueryParameters() {
        val absoluteString = Uri.parse("https://example.com/test?q=example/#1/anotherrandomcode").absoluteString
        assertEquals("https://example.com/test", absoluteString)
    }

    @Test
    fun whenNullUrlThenNullFaviconUrl() {
        assertNull("".toUri().faviconLocation())
    }

    @Test
    fun whenHttpRequestThenFaviconLocationAlsoHttp() {
        val favicon = "http://example.com".toUri().faviconLocation()
        assertTrue(favicon!!.isHttp)
    }

    @Test
    fun whenHttpsRequestThenFaviconLocationAlsoHttps() {
        val favicon = "https://example.com".toUri().faviconLocation()
        assertTrue(favicon!!.isHttps)
    }

    @Test
    fun whenUrlContainsASubdomainThenSubdomainReturnedInFavicon() {
        val favicon = "https://sub.example.com".toUri().faviconLocation()
        assertEquals("https://sub.example.com/favicon.ico", favicon.toString())
    }

    @Test
    fun whenUrlIsIpAddressThenIpReturnedInFaviconUrl() {
        val favicon = "https://192.168.1.0".toUri().faviconLocation()
        assertEquals("https://192.168.1.0/favicon.ico", favicon.toString())
    }

    @Test
    fun whenUrlDoesNotHaveSchemeReturnNull() {
        assertNull("www.example.com".toUri().domain())
    }

    @Test
    fun whenUrlHasSchemeReturnDomain() {
        assertEquals("www.example.com", "http://www.example.com".toUri().domain())
    }

    @Test
    fun whenUriHasResourceNameThenDropSchemeReturnResourceName() {
        assertEquals("www.foo.com", "https://www.foo.com".toUri().toStringDropScheme())
        assertEquals("www.foo.com", "http://www.foo.com".toUri().toStringDropScheme())
    }

    @Test
    fun whenUriHasResourceNameAndPathThenDropSchemeReturnResourceNameAndPath() {
        assertEquals("www.foo.com/path/to/foo", "https://www.foo.com/path/to/foo".toUri().toStringDropScheme())
        assertEquals("www.foo.com/path/to/foo", "http://www.foo.com/path/to/foo".toUri().toStringDropScheme())
    }

    @Test
    fun whenUriHasResourceNamePathAndParamsThenDropSchemeReturnResourceNamePathAndParams() {
        assertEquals("www.foo.com/path/to/foo?key=value", "https://www.foo.com/path/to/foo?key=value".toUri().toStringDropScheme())
        assertEquals("www.foo.com/path/to/foo?key=value", "http://www.foo.com/path/to/foo?key=value".toUri().toStringDropScheme())
    }

    @Test
    fun whenUriExtractDomainThenReturnDomainOnly() {
        assertEquals("www.foo.com", "https://www.foo.com/path/to/foo?key=value".extractDomain())
        assertEquals("www.foo.com", "www.foo.com/path/to/foo?key=value".extractDomain())
        assertEquals("foo.com", "foo.com/path/to/foo?key=value".extractDomain())
        assertEquals("foo.com", "http://foo.com/path/to/foo?key=value".extractDomain())
    }
}
