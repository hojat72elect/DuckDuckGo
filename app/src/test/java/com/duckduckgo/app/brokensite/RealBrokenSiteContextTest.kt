package com.duckduckgo.app.brokensite

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.browser.DuckDuckGoUrlDetector
import com.duckduckgo.browser.api.brokensite.BrokenSiteContext
import com.duckduckgo.browser.api.brokensite.BrokenSiteOpenerContext
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RealBrokenSiteContextTest {

    private lateinit var testee: BrokenSiteContext
    private val mockDuckDuckGoUrlDetector: DuckDuckGoUrlDetector = mock()

    @Before
    fun before() {
        testee = RealBrokenSiteContext(mockDuckDuckGoUrlDetector)
    }

    @Test
    fun whenInitializedThenUserRefreshCountIsZero() {
        assertEquals(0, testee.userRefreshCount)
    }

    @Test
    fun whenUserTriggersRefreshesThenUserRefreshCountIsIncremented() {
        testee.onUserTriggeredRefresh()
        assertEquals(1, testee.userRefreshCount)
    }

    @Test
    fun whenInitializedThenOpenerContextIsNull() {
        assertNull(testee.openerContext)
    }

    @Test
    fun whenReferrerIsDdgDomainAndNotExternallyLaunchedThenOpenerContextIsAssignedSerp() {
        val ddgUrl = "https://duckduckgo.com"
        val isExternallyLaunched = false
        whenever(mockDuckDuckGoUrlDetector.isDuckDuckGoUrl(ddgUrl)).thenReturn(true)

        testee.inferOpenerContext(ddgUrl, isExternallyLaunched)

        assertEquals(BrokenSiteOpenerContext.SERP, testee.openerContext)
    }

    @Test
    fun whenReferrerIsAnyNonDdgDomainAndNotExternallyLaunchedThenOpenerContextIsAssignedNavigation() {
        val url = "https://example.com"
        val isExternallyLaunched = false
        whenever(mockDuckDuckGoUrlDetector.isDuckDuckGoUrl(url)).thenReturn(false)

        testee.inferOpenerContext(url, isExternallyLaunched)

        assertEquals(BrokenSiteOpenerContext.NAVIGATION, testee.openerContext)
    }

    @Test
    fun whenLaunchedByExternalAppThenOpenerContextIsAssignedExternal() {
        val url = "https://example.com"
        val isExternallyLaunched = true
        whenever(mockDuckDuckGoUrlDetector.isDuckDuckGoUrl(url)).thenReturn(false)

        testee.inferOpenerContext(url, isExternallyLaunched)

        assertEquals(BrokenSiteOpenerContext.EXTERNAL, testee.openerContext)
    }

    @Test
    fun whenReferrerIsNullAndNotExternallyLaunchedThenOpenerContextIsNotAssigned() {
        val isExternallyLaunched = false

        testee.inferOpenerContext(null, isExternallyLaunched)

        assertNull(testee.openerContext)
    }

    @Test
    fun whenInitializedThenJsPerformanceIsNull() {
        assertNull(testee.jsPerformance)
    }

    @Test
    fun whenBreakageReportingDataIsNotNullJsPerformanceIsAssignedItsValue() {
        val jsonPerfData = JSONArray()
        jsonPerfData.put(123.45)

        testee.recordJsPerformance(jsonPerfData)

        doubleArrayOf(123.45).contentEquals(testee.jsPerformance)
    }
}
