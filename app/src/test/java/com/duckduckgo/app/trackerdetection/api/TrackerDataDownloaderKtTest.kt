

package com.duckduckgo.app.trackerdetection.api

import com.duckduckgo.common.utils.extensions.extractETag
import okhttp3.Headers
import org.junit.Assert.*
import org.junit.Test

class TrackerDataDownloaderKtTest {

    @Test
    fun whenExtractETagAndContainsPrefixAndQuotesThenReturnETag() {
        val headers = Headers.headersOf("eTag", "W/\"123456789\"")
        assertEquals(ETAG, headers.extractETag())
    }

    @Test
    fun whenExtractETagAndContainsQuotesThenReturnETag() {
        val headers = Headers.headersOf("eTag", "\"123456789\"")
        assertEquals(ETAG, headers.extractETag())
    }

    @Test
    fun whenExtractETagAndDoesNotContainsQuotesAndPrefixThenReturnETag() {
        val headers = Headers.headersOf("eTag", "123456789")
        assertEquals(ETAG, headers.extractETag())
    }

    companion object {
        const val ETAG = "123456789"
    }
}
