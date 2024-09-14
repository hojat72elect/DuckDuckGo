

package com.duckduckgo.app.trackerdetection.api

import com.duckduckgo.app.browser.Domain
import com.duckduckgo.app.trackerdetection.model.Action.BLOCK
import com.duckduckgo.app.trackerdetection.model.Action.UNSUPPORTED
import com.duckduckgo.app.trackerdetection.model.TdsTracker
import com.duckduckgo.common.test.FileUtilities.loadText
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TdsTrackerJsonTest {

    private val actionConverter = ActionJsonAdapter()
    private val moshi = Moshi.Builder().add(actionConverter).build()
    private val jsonAdapter: JsonAdapter<TdsJson> = moshi.adapter(TdsJson::class.java)

    @Test
    fun whenFormatIsValidThenTrackersAreCreated() {
        val json = loadText(javaClass.classLoader!!, "json/tds_trackers.json")
        val trackers = jsonAdapter.fromJson(json)!!.jsonToTrackers()
        assertEquals(3, trackers.count())
    }

    @Test
    fun whenFormatIsValidThenBasicElementsAreConvertedCorrectly() {
        val json = loadText(javaClass.classLoader!!, "json/tds_trackers.json")
        val trackers = jsonAdapter.fromJson(json)!!.jsonToTrackers()
        val tracker = trackers["1dmp.io"]
        assertEquals(TdsTracker(Domain("1dmp.io"), BLOCK, "CleverDATA LLC", listOf("Advertising"), arrayListOf()), tracker)
    }

    @Test
    fun whenTrackerHasInvalidDefaultActionThenTrackerConvertedCorrectly() {
        val json = loadText(javaClass.classLoader!!, "json/tds_trackers_action_invalid.json")
        val jsonTrackers = jsonAdapter.fromJson(json)!!
        val trackers = jsonTrackers.jsonToTrackers()
        val tracker = trackers["1dmp.io"]
        assertEquals(TdsTracker(Domain("1dmp.io"), UNSUPPORTED, "CleverDATA LLC", listOf("Advertising"), arrayListOf()), tracker)
    }

    @Test
    fun whenTrackerIsMissingDefaultActionThenTrackerNotCreated() {
        val json = loadText(javaClass.classLoader!!, "json/tds_trackers_action_missing.json")
        val trackers = jsonAdapter.fromJson(json)!!.jsonToTrackers()
        assertEquals(2, trackers.count())
        assertFalse(trackers.containsKey("1dmp.io"))
    }
}
