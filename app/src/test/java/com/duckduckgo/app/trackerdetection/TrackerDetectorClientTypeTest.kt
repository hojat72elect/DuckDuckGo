

package com.duckduckgo.app.trackerdetection

import android.net.Uri
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.adclick.api.AdClickManager
import com.duckduckgo.app.privacy.db.UserAllowListDao
import com.duckduckgo.app.trackerdetection.db.WebTrackersBlockedDao
import com.duckduckgo.app.trackerdetection.model.TrackerStatus
import com.duckduckgo.app.trackerdetection.model.TrackerType
import com.duckduckgo.app.trackerdetection.model.TrackingEvent
import com.duckduckgo.privacy.config.api.ContentBlocking
import com.duckduckgo.privacy.config.api.TrackerAllowlist
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class TrackerDetectorClientTypeTest {

    private val mockEntityLookup: EntityLookup = mock()
    private val mockBlockingClient: Client = mock()
    private val mockUserAllowListDao: UserAllowListDao = mock()
    private val mockWebTrackersBlockedDao: WebTrackersBlockedDao = mock()
    private val mockContentBlocking: ContentBlocking = mock()
    private val mockTrackerAllowlist: TrackerAllowlist = mock()
    private val mockAdClickManager: AdClickManager = mock()

    private val testee = TrackerDetectorImpl(
        mockEntityLookup,
        mockUserAllowListDao,
        mockContentBlocking,
        mockTrackerAllowlist,
        mockWebTrackersBlockedDao,
        mockAdClickManager,
    )

    @Before
    fun before() {
        whenever(mockUserAllowListDao.contains(any())).thenReturn(false)

        whenever(mockBlockingClient.matches(eq(Url.BLOCKED), any<Uri>(), anyMap())).thenReturn(Client.Result(matches = true, isATracker = true))
        whenever(mockBlockingClient.matches(eq(Url.UNLISTED), any<Uri>(), anyMap())).thenReturn(Client.Result(matches = false, isATracker = false))
        whenever(mockBlockingClient.name).thenReturn(Client.ClientName.TDS)
        testee.addClient(mockBlockingClient)
    }

    @Test
    fun whenUrlMatchesOnlyInBlockingClientThenEvaluateReturnsTrackingEvent() {
        val url = Url.BLOCKED
        val expected = TrackingEvent(
            documentUrl = documentUrl,
            trackerUrl = url,
            categories = null,
            entity = null,
            surrogateId = null,
            status = TrackerStatus.BLOCKED,
            type = TrackerType.OTHER,
        )
        assertEquals(expected, testee.evaluate(url, documentUrl.toUri(), requestHeaders = mapOf()))
    }

    @Test
    fun whenUrlDoesNotMatchInAnyClientsThenEvaluateReturnsAllowedDomain() {
        val url = Url.UNLISTED
        val expected = TrackingEvent(
            documentUrl = documentUrl,
            trackerUrl = url,
            categories = null,
            entity = null,
            surrogateId = null,
            status = TrackerStatus.ALLOWED,
            type = TrackerType.OTHER,
        )
        assertEquals(expected, testee.evaluate(url, documentUrl.toUri(), requestHeaders = mapOf()))
    }

    companion object {
        private const val documentUrl = "http://example.com"
    }

    object Url {
        const val BLOCKED = "blocked.com"
        const val UNLISTED = "unlisted.com"
    }
}
