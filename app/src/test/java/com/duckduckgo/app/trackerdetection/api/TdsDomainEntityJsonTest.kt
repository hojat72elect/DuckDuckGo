

package com.duckduckgo.app.trackerdetection.api

import com.duckduckgo.app.trackerdetection.model.TdsDomainEntity
import com.duckduckgo.common.test.FileUtilities.loadText
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TdsDomainEntityJsonTest {

    private val actionConverter = ActionJsonAdapter()
    private val moshi = Moshi.Builder().add(actionConverter).build()
    private val jsonAdapter: JsonAdapter<TdsJson> = moshi.adapter(TdsJson::class.java)

    @Test
    fun whenFormatIsValidThenDomainEntitiesAreCreated() {
        val json = loadText(javaClass.classLoader!!, "json/tds_domain_entities.json")
        val domains = jsonAdapter.fromJson(json)!!.jsonToDomainEntities()
        assertEquals(3, domains.count())
    }

    @Test
    fun whenFormatIsValidThenDomainEntitiesAreConvertedCorrectly() {
        val json = loadText(javaClass.classLoader!!, "json/tds_domain_entities.json")
        val domains = jsonAdapter.fromJson(json)!!.jsonToDomainEntities()
        val domain = domains.first()
        assertEquals(TdsDomainEntity("truoptik.com", "21 Productions Inc"), domain)
    }

    @Test
    fun whenValueIsNullThenDomainEntitiesNotCreated() {
        val json = loadText(javaClass.classLoader!!, "json/tds_domain_entities_null_value.json")
        val domains = jsonAdapter.fromJson(json)!!.jsonToDomainEntities()
        assertEquals(2, domains.count())
        assertNull(domains.firstOrNull { it.domain == "33across.com" })
    }
}
