

package com.duckduckgo.app.global

import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.browser.UriString.Companion.isWebUrl
import com.duckduckgo.app.browser.UriString.Companion.sameOrSubdomain
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UriStringTest {

    @Test
    fun whenUrlsHaveSameDomainThenSameOrSubdomainIsTrue() {
        assertTrue(sameOrSubdomain("http://example.com/index.html", "http://example.com/home.html"))
    }

    @Test
    fun whenUrlsHaveSameDomainThenSameOrSubdomainIsTrue2() {
        assertTrue(sameOrSubdomain("http://example.com/index.html".toUri(), "http://example.com/home.html"))
    }

    @Test
    fun whenUrlIsSubdomainThenSameOrSubdomainIsTrue() {
        assertTrue(sameOrSubdomain("http://subdomain.example.com/index.html", "http://example.com/home.html"))
    }

    @Test
    fun whenUrlIsSubdomainThenSameOrSubdomainIsTrue2() {
        assertTrue(sameOrSubdomain("http://subdomain.example.com/index.html".toUri(), "http://example.com/home.html"))
    }

    @Test
    fun whenUrlIsAParentDomainThenSameOrSubdomainIsFalse() {
        assertFalse(sameOrSubdomain("http://example.com/index.html", "http://parent.example.com/home.html"))
    }

    @Test
    fun whenUrlIsAParentDomainThenSameOrSubdomainIsFalse2() {
        assertFalse(sameOrSubdomain("http://example.com/index.html".toUri(), "http://parent.example.com/home.html"))
    }

    @Test
    fun whenChildUrlIsMalformedThenSameOrSubdomainIsFalse() {
        assertFalse(sameOrSubdomain("??.example.com/index.html", "http://example.com/home.html"))
    }

    @Test
    fun whenChildUrlIsMalformedThenSameOrSubdomainIsFalse2() {
        assertFalse(sameOrSubdomain("??.example.com/index.html".toUri(), "http://example.com/home.html"))
    }

    @Test
    fun whenParentUrlIsMalformedThenSameOrSubdomainIsFalse() {
        assertFalse(sameOrSubdomain("http://example.com/index.html", "??.example.com/home.html"))
    }

    @Test
    fun whenParentUrlIsMalformedThenSameOrSubdomainIsFalse2() {
        assertFalse(sameOrSubdomain("http://example.com/index.html".toUri(), "??.example.com/home.html"))
    }

    @Test
    fun whenUrlsHaveSameDomainThenSafeSameOrSubdomainIsTrue() {
        assertTrue(sameOrSubdomain("http://example.com/index.html", "http://example.com/home.html"))
    }

    @Test
    fun whenUrlsHaveSameDomainThenSafeSameOrSubdomainIsTrue2() {
        assertTrue(sameOrSubdomain("http://example.com/index.html".toUri(), "http://example.com/home.html"))
    }

    @Test
    fun whenUrlIsSubdomainThenSafeSameOrSubdomainIsTrue() {
        assertTrue(sameOrSubdomain("http://subdomain.example.com/index.html", "http://example.com/home.html"))
    }

    @Test
    fun whenUrlIsSubdomainThenSafeSameOrSubdomainIsTrue2() {
        assertTrue(sameOrSubdomain("http://subdomain.example.com/index.html".toUri(), "http://example.com/home.html"))
    }

    @Test
    fun whenUrlIsAParentDomainThenSafeSameOrSubdomainIsFalse() {
        assertFalse(sameOrSubdomain("http://example.com/index.html", "http://parent.example.com/home.html"))
    }

    @Test
    fun whenUrlIsAParentDomainThenSafeSameOrSubdomainIsFalse2() {
        assertFalse(sameOrSubdomain("http://example.com/index.html".toUri(), "http://parent.example.com/home.html"))
    }

    @Test
    fun whenChildUrlIsMalformedThenSafeSameOrSubdomainIsFalse() {
        assertFalse(sameOrSubdomain("??.example.com/index.html", "http://example.com/home.html"))
    }

    @Test
    fun whenChildUrlIsMalformedThenSafeSameOrSubdomainIsFalse2() {
        assertFalse(sameOrSubdomain("??.example.com/index.html".toUri(), "http://example.com/home.html"))
    }

    @Test
    fun whenParentUrlIsMalformedThenSafeSameOrSubdomainIsFalse() {
        assertFalse(sameOrSubdomain("http://example.com/index.html", "??.example.com/home.html"))
    }

    @Test
    fun whenParentUrlIsMalformedThenSafeSameOrSubdomainIsFalse2() {
        assertFalse(sameOrSubdomain("http://example.com/index.html".toUri(), "??.example.com/home.html"))
    }

    @Test
    fun whenUserIsPresentThenIsWebUrlIsFalse() {
        val input = "http://example.com@sample.com"
        assertFalse(isWebUrl(input))
    }

    @Test
    fun whenGivenLongWellFormedUrlThenIsWebUrlIsTrue() {
        val input = "http://www.veganchic.com/products/Camo-High-Top-Sneaker-by-The-Critical-Slide-Societ+80758-0180.html"
        assertTrue(isWebUrl(input))
    }

    @Test
    fun whenHostIsValidThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("test.com"))
    }

    @Test
    fun whenHostIsValidIpAddressThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("121.33.2.11"))
    }

    @Test
    fun whenHostIsValidIpAddressWithPortThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("121.33.2.11:999"))
    }

    @Test
    fun whenHostIsLocalhostThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("localhost"))
    }

    @Test
    fun whenHostIsInvalidContainsSpaceThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("t est.com"))
    }

    @Test
    fun whenHostIsInvalidContainsExclamationMarkThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("test!com.com"))
    }

    @Test
    fun whenHostIsInvalidIpThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("121.33.33."))
    }

    @Test
    fun whenHostIsInvalidMisspelledLocalhostContainsSpaceThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("localhostt"))
    }

    @Test
    fun whenSchemeIsValidNormalUrlThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://test.com"))
    }

    @Test
    fun whenSchemeIsValidIpAddressThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://121.33.2.11"))
    }

    @Test
    fun whenSchemeIsValidIpAddressWithPortThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://121.33.2.11:999"))
    }

    @Test
    fun whenSchemeIsValidLocalhostUrlThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://localhost"))
    }

    @Test
    fun whenSchemeIsInvalidNormalUrlThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("asdas://test.com"))
    }

    @Test
    fun whenSchemeIsInvalidIpAddressThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("asdas://121.33.2.11"))
    }

    @Test
    fun whenSchemeIsInvalidLocalhostThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("asdas://localhost"))
    }

    @Test
    fun whenTextIsIncompleteHttpSchemeLettersOnlyThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("http"))
    }

    @Test
    fun whenTextIsIncompleteHttpSchemeMissingBothSlashesThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("http:"))
    }

    @Test
    fun whenTextIsIncompleteHttpSchemeMissingOneSlashThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("http:/"))
    }

    @Test
    fun whenTextIsIncompleteHttpsSchemeLettersOnlyThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("https"))
    }

    @Test
    fun whenTextIsIncompleteHttpsSchemeMissingBothSlashesThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("https:"))
    }

    @Test
    fun whenTextIsIncompleteHttpsSchemeMissingOneSlashThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("https:/"))
    }

    @Test
    fun whenPathIsValidNormalUrlThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://test.com/path"))
    }

    @Test
    fun whenPathIsValidIpAddressThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://121.33.2.11/path"))
    }

    @Test
    fun whenPathIsValidIpAddressWithPortThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://121.33.2.11:999/path"))
    }

    @Test
    fun whenPathIsValidLocalhostThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://localhost/path"))
    }

    @Test
    fun whenPathIsValidMissingSchemeNormalUrlThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("test.com/path"))
    }

    @Test
    fun whenPathIsValidMissingSchemeIpAddressThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("121.33.2.11/path"))
    }

    @Test
    fun whenPathIsValidMissingSchemeLocalhostThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("localhost/path"))
    }

    @Test
    fun whenPathIsInvalidContainsSpaceNormalUrlThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("http://test.com/pa th"))
    }

    @Test
    fun whenPathIsInvalidContainsSpaceIpAddressThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("http://121.33.2.11/pa th"))
    }

    @Test
    fun whenPathIsInvalidContainsSpaceLocalhostThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("http://localhost/pa th"))
    }

    @Test
    fun whenPathIsInvalidContainsSpaceMissingSchemeNormalUrlThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("test.com/pa th"))
    }

    @Test
    fun whenPathIsInvalidContainsSpaceMissingSchemeIpAddressThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("121.33.2.11/pa th"))
    }

    @Test
    fun whenPathIsInvalidContainsSpaceMissingSchemeLocalhostThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("localhost/pa th"))
    }

    @Test
    fun whenPathIsValidContainsEncodedSpaceNormalUrlThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://www.example.com/pa%20th"))
    }

    @Test
    fun whenParamsAreValidNormalUrlThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://test.com?s=dafas&d=342"))
    }

    @Test
    fun whenParamsAreValidIpAddressThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://121.33.2.11?s=dafas&d=342"))
    }

    @Test
    fun whenParamsAreValidLocalhostThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("http://localhost?s=dafas&d=342"))
    }

    @Test
    fun whenParamsAreValidNormalUrlMissingSchemeThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("test.com?s=dafas&d=342"))
    }

    @Test
    fun whenParamsAreValidIpAddressMissingSchemeThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("121.33.2.11?s=dafas&d=342"))
    }

    @Test
    fun whenParamsAreValidLocalhostMissingSchemeThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("localhost?s=dafas&d=342"))
    }

    @Test
    fun whenParamsAreValidContainsEncodedUriThenIsWebUrlIsTrue() {
        assertTrue(isWebUrl("https://m.facebook.com/?refsrc=https%3A%2F%2Fwww.facebook.com%2F&_rdr"))
    }

    @Test
    fun whenGivenSimpleStringThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("randomtext"))
    }

    @Test
    fun whenGivenStringWithDotPrefixThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl(".randomtext"))
    }

    @Test
    fun whenGivenStringWithDotSuffixThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("randomtext."))
    }

    @Test
    fun whenGivenNumberThenIsWebUrlIsFalse() {
        assertFalse(isWebUrl("33"))
    }

    @Test
    fun whenNamedLocalMachineWithSchemeAndPortThenIsTrue() {
        assertTrue(isWebUrl("http://raspberrypi:8080"))
    }

    @Test
    fun whenNamedLocalMachineWithNoSchemeAndPortThenIsFalse() {
        assertFalse(isWebUrl("raspberrypi:8080"))
    }

    @Test
    fun whenNamedLocalMachineWithSchemeNoPortThenIsTrue() {
        assertTrue(isWebUrl("http://raspberrypi"))
    }

    @Test
    fun whenStartsWithSiteSpecificSearchThenIsFalse() {
        assertFalse(isWebUrl("site:example.com"))
    }

    @Test
    fun whenSchemeIsValidFtpButNotHttpThenIsFalse() {
        assertFalse(isWebUrl("ftp://example.com"))
    }

    @Test
    fun whenUrlStartsWithDoubleQuoteThenIsFalse() {
        assertFalse(isWebUrl("\"example.com"))
    }

    @Test
    fun whenUrlStartsWithSingleQuoteThenIsFalse() {
        assertFalse(isWebUrl("'example.com"))
    }

    @Test
    fun whenUrlEndsWithDoubleQuoteThenIsFalse() {
        assertFalse(isWebUrl("example.com\""))
    }

    @Test
    fun whenUrlEndsWithSingleQuoteThenIsFalse() {
        assertFalse(isWebUrl("example.com'"))
    }

    @Test
    fun whenUrlStartsAndEndsWithDoubleQuoteThenIsFalse() {
        assertFalse(isWebUrl("\"example.com\""))
    }

    @Test
    fun whenUrlStartsAndEndsWithSingleQuoteThenIsFalse() {
        assertFalse(isWebUrl("'example.com'"))
    }

    @Test
    fun whenUrlContainsDoubleQuoteThenIsFalse() {
        assertFalse(isWebUrl("example\".com"))
    }

    @Test
    fun whenUrlContainsSingleQuoteThenIsFalse() {
        assertFalse(isWebUrl("example'.com"))
    }
}
