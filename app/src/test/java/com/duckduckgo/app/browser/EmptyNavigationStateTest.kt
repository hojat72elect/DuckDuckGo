

package com.duckduckgo.app.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EmptyNavigationStateTest {

    @Test
    fun whenEmptyNavigationStateFromNavigationStateThenBrowserPropertiesAreTheSame() {
        val previousState = buildState("originalUrl", "currentUrl", "titlle")
        val emptyNavigationState = EmptyNavigationState(previousState)

        assertEquals(emptyNavigationState.currentUrl, previousState.currentUrl)
        assertEquals(emptyNavigationState.originalUrl, previousState.originalUrl)
        assertEquals(emptyNavigationState.title, previousState.title)
    }

    @Test
    fun whenEmptyNavigationStateFromNavigationStateThenNavigationPropertiesAreCleared() {
        val emptyNavigationState = EmptyNavigationState(buildState("originalUrl", "currentUrl", "titlle"))

        assertEquals(emptyNavigationState.stepsToPreviousPage, 0)
        assertFalse(emptyNavigationState.canGoBack)
        assertFalse(emptyNavigationState.canGoForward)
        assertFalse(emptyNavigationState.hasNavigationHistory)
    }

    private fun buildState(
        originalUrl: String?,
        currentUrl: String?,
        title: String? = null,
    ): WebNavigationState {
        return TestNavigationState(
            originalUrl = originalUrl,
            currentUrl = currentUrl,
            title = title,
            stepsToPreviousPage = 1,
            canGoBack = true,
            canGoForward = true,
            hasNavigationHistory = true,
            progress = null,
        )
    }
}
