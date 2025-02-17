

package com.duckduckgo.app.tabs.ui

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.tabs.model.TabEntity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class TabRendererExtensionTest {

    private val context: Context = mock()

    @Test
    fun whenTabIsBlankThenDisplayTitleIsDuckDuckGo() {
        whenever(context.getString(R.string.homeTab)).thenReturn("DuckDuckGo")
        assertEquals("DuckDuckGo", TabEntity("", position = 0).displayTitle(context))
    }

    @Test
    fun whenTabHasTitleThenDisplayTitleIsSame() {
        assertEquals(TITLE, TabEntity("", URL, TITLE, position = 0).displayTitle(context))
    }

    @Test
    fun whenTabDoesNotHaveTitleThenDisplayTitleIsUrlHost() {
        assertEquals("example.com", TabEntity("", URL, null, position = 0).displayTitle(context))
    }

    @Test
    fun whenTabDoesNotHaveTitleAndUrlIsInvalidThenTitleIsBlank() {
        assertEquals("", TabEntity("", INVALID_URL, null, position = 0).displayTitle(context))
    }

    @Test
    fun whenTabIsBlankThenUrlIsDuckDuckGo() {
        assertEquals("https://duckduckgo.com", TabEntity("", position = 0).displayUrl())
    }

    @Test
    fun whenTabHasUrlThenDisplayUrlIsSame() {
        assertEquals(URL, TabEntity("", URL, TITLE, position = 0).displayUrl())
    }

    @Test
    fun whenTabDoesNotHaveAUrlThenDisplayUrlIsBlank() {
        assertEquals("", TabEntity("", null, TITLE, position = 0).displayUrl())
    }

    companion object {
        private const val TITLE = "Title"
        private const val URL = "https://example.com"
        private const val INVALID_URL = "notaurl"
    }
}
