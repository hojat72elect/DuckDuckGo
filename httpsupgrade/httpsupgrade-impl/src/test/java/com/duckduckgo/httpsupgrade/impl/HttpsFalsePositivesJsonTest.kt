

package com.duckduckgo.httpsupgrade.impl

import com.duckduckgo.httpsupgrade.store.HttpsFalsePositiveDomain
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpsFalsePositivesJsonTest {

    @Test
    fun whenGivenValidJsonThenParsesCorrectly() {
        val moshi = Moshi.Builder().add(HttpsFalsePositivesJsonAdapter()).build()
        val type = Types.newParameterizedType(List::class.java, HttpsFalsePositiveDomain::class.java)
        val jsonAdapter: JsonAdapter<List<HttpsFalsePositiveDomain>> = moshi.adapter(type)

        val list = jsonAdapter.fromJson(json())!!
        assertEquals(7, list.count())
    }

    private fun json(): String = """
        {
            "data": [
                "mlb.mlb.com",
                "proa.accuweather.com",
                "jbhard.org",
                "lody.net",
                "ci.brookfield.wi.us",
                "cocktaildreams.de",
                "anwalt-im-netz.de"
            ]
        }
        """
}
