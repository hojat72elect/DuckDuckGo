

package com.duckduckgo.app.browser

import android.graphics.Bitmap
import android.webkit.WebBackForwardList
import android.webkit.WebHistoryItem
import org.junit.Assert.assertEquals
import org.junit.Test

class WebViewNavigationStateTest {

    private val stack: TestBackForwardList = TestBackForwardList()

    @Test
    fun whenWebHistoryIsEmptyThenNavigationHistoryIsEmpty() {
        val stack = webViewState(stack).navigationHistory
        assertEquals(0, stack.size)
    }

    @Test
    fun whenWebHistoryHasSinglePageThenNavigationHistoryRetrieved() {
        stack.addPageToHistory("example.com".toHistoryItem())
        val stack = webViewState(stack).navigationHistory
        assertEquals(1, stack.size)
        assertEquals("example.com", stack[0].url)
    }

    @Test
    fun whenWebHistoryHasMultiplePagesThenNavigationHistoryRetrievedInCorrectOrder() {
        stack.addPageToHistory("a".toHistoryItem())
        stack.addPageToHistory("b".toHistoryItem())
        stack.addPageToHistory("c".toHistoryItem())

        val stack = webViewState(stack).navigationHistory

        assertEquals(3, stack.size)
        assertEquals("c", stack[0].url)
        assertEquals("b", stack[1].url)
        assertEquals("a", stack[2].url)
    }

    private fun webViewState(stack: WebBackForwardList): WebViewNavigationState {
        return WebViewNavigationState(stack = stack, progress = null)
    }

    private fun String.toHistoryItem(): WebHistoryItem {
        return TestHistoryItem(this)
    }
}

private class TestBackForwardList : WebBackForwardList() {

    private val fakeHistory: MutableList<WebHistoryItem> = mutableListOf()
    private var fakeCurrentIndex = -1

    fun addPageToHistory(webHistoryItem: WebHistoryItem) {
        fakeHistory.add(webHistoryItem)
        fakeCurrentIndex++
    }

    override fun getSize() = fakeHistory.size

    override fun getItemAtIndex(index: Int): WebHistoryItem = fakeHistory[index]

    override fun getCurrentItem(): WebHistoryItem? = null

    override fun getCurrentIndex(): Int = fakeCurrentIndex

    override fun clone(): WebBackForwardList = throw NotImplementedError()
}

private class TestHistoryItem(
    private val url: String,
) : WebHistoryItem() {

    override fun getUrl(): String = url

    override fun getOriginalUrl(): String = url

    override fun getTitle(): String = url

    override fun getFavicon(): Bitmap = throw NotImplementedError()

    override fun clone(): WebHistoryItem = throw NotImplementedError()
}
