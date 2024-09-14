

package com.duckduckgo.app.global

import com.duckduckgo.common.utils.sha1
import com.duckduckgo.common.utils.sha256
import com.duckduckgo.common.utils.verifySha256
import org.junit.Assert.*
import org.junit.Test

class HashUtilitiesTest {

    @Test
    fun whenSha1HashCalledOnStringThenResultIsCorrect() {
        val result = helloWorldText.sha1
        assertEquals(helloWorldSha1, result)
    }

    @Test
    fun whenSha256HashCalledOnBytesThenResultIsCorrect() {
        val result = helloWorldText.toByteArray().sha256
        assertEquals(helloWorldSha256, result)
    }

    @Test
    fun whenSha256HashCalledOnStringThenResultIsCorrect() {
        val result = helloWorldText.sha256
        assertEquals(helloWorldSha256, result)
    }

    @Test
    fun whenCorrectSha256HashUsedThenVerifyIsTrue() {
        assertTrue(helloWorldText.toByteArray().verifySha256(helloWorldSha256))
    }

    @Test
    fun whenIncorrectByteSha256HashUsedThenVerifyIsFalse() {
        assertFalse(helloWorldText.toByteArray().verifySha256(otherSha256))
    }

    companion object {
        const val helloWorldText = "Hello World!"
        const val helloWorldSha256 = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"
        const val helloWorldSha1 = "2ef7bde608ce5404e97d5f042f95f89f1c232871"
        const val otherSha256 = "f97e9da0e3b879f0a9df979ae260a5f7e1371edb127c1862d4f861981166cdc1"
    }
}
