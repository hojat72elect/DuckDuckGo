

package com.duckduckgo.app.browser.downloader

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BlobConverterJavascriptInterfaceTest {

    lateinit var testee: BlobConverterJavascriptInterface
    var result: String = ""

    @Before
    fun setup() {
        testee = BlobConverterJavascriptInterface { first, second -> result = first + second }
    }

    @Test
    fun whenConvertToBlobDataUriThenLambdaCalled() {
        testee.convertBlobToDataUri("first", "second")

        assertEquals("firstsecond", result)
    }
}
