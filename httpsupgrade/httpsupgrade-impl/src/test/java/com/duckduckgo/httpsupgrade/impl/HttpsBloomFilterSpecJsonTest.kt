

package com.duckduckgo.httpsupgrade.impl

import com.duckduckgo.httpsupgrade.store.HttpsBloomFilterSpec
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpsBloomFilterSpecJsonTest {

    @Test
    fun whenGivenValidJsonThenParsesCorrectly() {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(HttpsBloomFilterSpec::class.java)
        val result = jsonAdapter.fromJson(json())!!
        assertEquals(2858372, result.totalEntries)
        assertEquals(0.0001, result.errorRate, 0.00001)
        assertEquals("932ae1481fc33d94320a3b072638c0df8005482506933897e35feb1294693c84", result.sha256)
    }

    private fun json(): String = """
        {
          "totalEntries":2858372,
          "errorRate" : 0.0001,
          "sha256" : "932ae1481fc33d94320a3b072638c0df8005482506933897e35feb1294693c84"
        }
        """
}
