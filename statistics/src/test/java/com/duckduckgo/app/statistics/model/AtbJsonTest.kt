

package com.duckduckgo.app.statistics.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.common.test.FileUtilities.loadText
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AtbJsonTest {

    private val moshi = Moshi.Builder().build()
    private val jsonAdapter: JsonAdapter<Atb> = moshi.adapter(Atb::class.java)

    @Test
    fun whenFormatIsValidThenDataIsConverted() {
        val json = loadText(javaClass.classLoader!!, "json/atb_response_valid.json")
        val atb = jsonAdapter.fromJson(json)!!
        assertEquals("v105-3", atb.version)
    }

    @Test(expected = JsonEncodingException::class)
    fun whenFormatIsInvalidThenExceptionIsThrown() {
        assertNull(jsonAdapter.fromJson("invalid"))
    }
}
