

package com.duckduckgo.app.trackerdetection

import android.os.Build
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test

class RealUrlToTypeMapperTest {

    private lateinit var testee: UrlToTypeMapper
    private var requestHeaders = mutableMapOf<String, String>()

    @Before
    fun setup() {
        testee = RealUrlToTypeMapper()
        givenAcceptHeadersIsWildcard()
    }

    @Test
    fun whenUrlHasNoExtensionThenReturnNull() {
        assertNull(testee.map("example.com", requestHeaders))
    }

    @Test
    fun whenUrlHasImageExtensionThenReturnImageType() {
        assertEquals("image", testee.map("example.com/test.jpg", requestHeaders))
        assertEquals("image", testee.map("example.com/test.png", requestHeaders))
        assertEquals("image", testee.map("example.com/test.gif", requestHeaders))
        assertEquals("image", testee.map("example.com/test.svg", requestHeaders))
        assertEquals("image", testee.map("example.com/test.bmp", requestHeaders))
        assertEquals("image", testee.map("example.com/test.tif", requestHeaders))
    }

    @Test
    fun whenUrlHasScriptExtensionThenReturnScriptType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            assertEquals("script", testee.map("example.com/test.js", requestHeaders))
        }
    }

    @Test
    fun whenUrlHasStylesheetExtensionThenReturnStylesheetType() {
        assertEquals("stylesheet", testee.map("example.com/test.css", requestHeaders))
    }

    @Test
    fun whenAcceptHeadersAreAmbiguousThenFallbackToExtensionCheck() {
        requestHeaders["Accept"] = "image/avif,image/webp,image/apng,image/svg+xml,image/*,text/css,*/*;q=0.8"
        assertEquals("stylesheet", testee.map("example.com/test.css", requestHeaders))
    }

    @Test
    fun whenAcceptHeadersAreAmbiguousAndNoExtensionThenReturnNull() {
        requestHeaders["Accept"] = "image/avif,image/webp,image/apng,image/svg+xml,image/*,text/css,*/*;q=0.8"
        assertEquals(null, testee.map("example.com/test", requestHeaders))
    }

    @Test
    fun whenAcceptHeadersAreImageThenReturnImageType() {
        requestHeaders["Accept"] = "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"
        assertEquals("image", testee.map("example.com/test", requestHeaders))
    }

    @Test
    fun whenAcceptHeadersAreStylesheetThenReturnStylesheetType() {
        requestHeaders["Accept"] = "text/css,*/*;q=0.8"
        assertEquals("stylesheet", testee.map("example.com/test", requestHeaders))
    }

    @Test
    fun whenAcceptHeadersAreScriptThenReturnScriptType() {
        requestHeaders["Accept"] = "application/javascript,*/*;q=0.5"
        assertEquals("script", testee.map("example.com/test", requestHeaders))
    }

    @Test
    fun whenXRequestedWithHeadersAreXmlHttpRequestThenReturnXmlHttpRequestType() {
        requestHeaders["X-Requested-With"] = "XMLHttpRequest"
        assertEquals("xmlhttprequest", testee.map("example.com/test", requestHeaders))
    }

    private fun givenAcceptHeadersIsWildcard() {
        requestHeaders["Accept"] = "*/*"
    }
}
