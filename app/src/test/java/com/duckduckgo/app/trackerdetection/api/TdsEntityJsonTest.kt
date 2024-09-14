

package com.duckduckgo.app.trackerdetection.api

import com.duckduckgo.app.trackerdetection.model.TdsEntity
import com.duckduckgo.common.test.FileUtilities.loadText
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Test

class TdsEntityJsonTest {

    private val actionConverter = ActionJsonAdapter()
    private val moshi = Moshi.Builder().add(actionConverter).build()
    private val jsonAdapter: JsonAdapter<TdsJson> = moshi.adapter(TdsJson::class.java)

    @Test
    fun whenFormatIsValidThenEntitiesAreCreated() {
        val json = loadText(javaClass.classLoader!!, "json/tds_entities.json")
        val entities = jsonAdapter.fromJson(json)!!.jsonToEntities()
        assertEquals(4, entities.count())
    }

    @Test
    fun whenFormatIsValidThenBasicElementsAreConvertedCorrectly() {
        val json = loadText(javaClass.classLoader!!, "json/tds_entities.json")
        val entities = jsonAdapter.fromJson(json)!!.jsonToEntities()
        val entity = entities.first()
        assertEquals(TdsEntity("21 Productions Inc", "21 Productions", 0.348), entity)
    }

    @Test
    fun whenEntityIsMissingPrevalenceThenPrevalenceIsSetToZero() {
        val json = loadText(javaClass.classLoader!!, "json/tds_entities.json")
        val entities = jsonAdapter.fromJson(json)!!.jsonToEntities()
        val entity = entities[1]
        assertEquals(0.0, entity.prevalence, 0.0001)
    }

    @Test
    fun whenEntityIsMissingDisplayNameThenDisplayNameIsSameAsName() {
        val json = loadText(javaClass.classLoader!!, "json/tds_entities.json")
        val entities = jsonAdapter.fromJson(json)!!.jsonToEntities()
        val entity = entities[2]
        assertEquals("4Cite Marketing", entity.displayName)
    }

    @Test
    fun whenEntityHasBlankDisplayNameThenDisplayNameIsSameAsName() {
        val json = loadText(javaClass.classLoader!!, "json/tds_entities.json")
        val entities = jsonAdapter.fromJson(json)!!.jsonToEntities()
        val entity = entities.last()
        assertEquals("AT Internet", entity.displayName)
    }
}
