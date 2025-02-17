

package com.duckduckgo.app.browser

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.browser.WebNavigationStateChange.NewPage
import com.duckduckgo.app.browser.WebNavigationStateChange.Other
import com.duckduckgo.app.browser.WebNavigationStateChange.PageCleared
import com.duckduckgo.app.browser.WebNavigationStateChange.PageNavigationCleared
import com.duckduckgo.app.browser.WebNavigationStateChange.Unchanged
import com.duckduckgo.app.browser.WebNavigationStateChange.UrlUpdated
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebNavigationStateComparisonTest {

    @Test
    fun whenPreviousStateAndLatestStateSameThenCompareReturnsUnchanged() {
        val state = buildState("http://foo.com", "http://subdomain.foo.com")
        assertEquals(Unchanged, state.compare(state))
    }

    @Test
    fun whenPreviousStateAndLatestStateEqualThenCompareReturnsUnchanged() {
        val previousState = buildState("http://foo.com", "http://subdomain.foo.com")
        val latestState = buildState("http://foo.com", "http://subdomain.foo.com")
        assertEquals(Unchanged, latestState.compare(previousState))
    }

    @Test
    fun whenPreviousStateIsNullAndLatestContainsAnOriginalUrlACurrentUrlAndTitleThenCompareReturnsNewPageWithTitle() {
        val previousState = null
        val latestState = buildState("http://latest.com", "http://subdomain.latest.com", "Title")
        assertEquals(NewPage("http://subdomain.latest.com", "Title"), latestState.compare(previousState))
    }

    @Test
    fun whenPreviousStateIsNullAndLatestContainsAnOriginalUrlACurrentUrlAndNoTitleThenCompareReturnsNewPageWithoutTitle() {
        val previousState = null
        val latestState = buildState("http://latest.com", "http://subdomain.latest.com")
        assertEquals(NewPage("http://subdomain.latest.com", null), latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsNoOriginalOrCurrentUrlAndLatestContainsAnOriginalAndCurrentUrlThenCompareReturnsNewPage() {
        val previousState = buildState(null, null)
        val latestState = buildState("http://latest.com", "http://subdomain.latest.com")
        assertEquals(NewPage("http://subdomain.latest.com", null), latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsNoOriginalUrlAndACurrentUrlAndLatestContainsAnOriginalAndCurrentUrlThenCompareReturnsNewPage() {
        val previousState = buildState(null, "http://subdomain.previous.com")
        val latestState = buildState("http://latest.com", "http://subdomain.latest.com")
        assertEquals(NewPage("http://subdomain.latest.com", null), latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsAnOriginalUrlAndNoCurrentUrlAndLatestContainsAnOriginalAndCurrentUrlThenCompareReturnsNewPage() {
        val previousState = buildState("http://previous.com", null)
        val latestState = buildState("http://latest.com", "http://subdomain.latest.com")
        assertEquals(NewPage("http://subdomain.latest.com", null), latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsAnOriginalUrlAndCurrentUrlAndLatestContainsADifferentOriginalUrlThenCompareReturnsNewPage() {
        val previousState = buildState("http://previous.com", "http://subdomain.previous.com")
        val latestState = buildState("http://latest.com", "http://subdomain.latest.com")
        assertEquals(NewPage("http://subdomain.latest.com", null), latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsAnOriginalUrlAndCurrentUrlAndLatestContainsSameOriginalUrlAndDifferentCurrentUrlDomainThenCompareReturnsNewPage() {
        val previousState = buildState("http://same.com", "http://subdomain.previous.com")
        val latestState = buildState("http://same.com", "http://subdomain.latest.com")
        assertEquals(NewPage("http://subdomain.latest.com", null), latestState.compare(previousState))
    }

    @Test
    fun whenPrevContainsAnOriginalUrlAndCurrentUrlAndLatestContainsSameOriginalUrlAndDifferentCurrentUrlWithSameHostThenCompareReturnsUrlUpdated() {
        val previousState = buildState("http://same.com", "http://same.com/previous")
        val latestState = buildState("http://same.com", "http://same.com/latest")
        assertEquals(UrlUpdated("http://same.com/latest"), latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsAnOriginalUrlAndCurrentUrlAndLatestStateContainsNoOriginalUrlAndNoCurrentUrlThenCompareReturnsPageCleared() {
        val previousState = buildState("http://previous.com", "http://subdomain.previous.com")
        val latestState = buildState(null, null)
        assertEquals(PageCleared, latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsAnOriginalUrlAndCurrentUrlAndLatestStateContainsNoOriginalUrlAndACurrentUrlThenCompareReturnsPageCleared() {
        val previousState = buildState("http://previous.com", "http://subdomain.previous.com")
        val latestState = buildState(null, "http://subdomain.latest.com")
        assertEquals(PageCleared, latestState.compare(previousState))
    }

    @Test
    fun whenLatestStateIsEmptyNavigationCompareReturnsPageNavigationCleared() {
        val previousState = buildState("http://previous.com", "http://subdomain.previous.com")
        val latestState = EmptyNavigationState(previousState)
        assertEquals(PageNavigationCleared, latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsAnOriginalUrlAndCurrentUrlAndLatestContainsSameOriginalUrlAndNoCurrentUrlThenCompareReturnsOther() {
        val previousState = buildState("http://same.com", "http://subdomain.previous.com")
        val latestState = buildState("http://same.com", null)
        assertEquals(Other, latestState.compare(previousState))
    }

    @Test
    fun whenPreviousContainsAnOriginalUrlAndCurrentUrlAndLatestStateContainsDifferentOriginalUrlAndNoCurrentUrlThenCompareReturnsOther() {
        val previousState = buildState("http://previous.com", "http://subdomain.previous.com")
        val latestState = buildState("http://latest.com", null)
        assertEquals(Other, latestState.compare(previousState))
    }

    private fun buildState(
        originalUrl: String?,
        currentUrl: String?,
        title: String? = null,
        newProgress: Int? = null,
    ): WebNavigationState {
        return TestNavigationState(
            originalUrl = originalUrl,
            currentUrl = currentUrl,
            title = title,
            stepsToPreviousPage = 1,
            canGoBack = true,
            canGoForward = true,
            hasNavigationHistory = true,
            progress = newProgress,
        )
    }
}
