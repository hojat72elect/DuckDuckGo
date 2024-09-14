

package com.duckduckgo.privacy.config.impl.features.trackerallowlist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.privacy.config.store.TrackerAllowlistEntity
import com.duckduckgo.privacy.config.store.features.trackerallowlist.TrackerAllowlistRepository
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RealTrackerAllowlistTest {

    private val mockTrackerAllowlistRepository: TrackerAllowlistRepository = mock()
    private val mockFeatureToggle: FeatureToggle = mock()
    private lateinit var testee: RealTrackerAllowlist

    @Before
    fun setup() {
        whenever(mockFeatureToggle.isFeatureEnabled(any(), any())).thenReturn(true)

        testee = RealTrackerAllowlist(mockTrackerAllowlistRepository, mockFeatureToggle)
    }

    @Test
    fun whenUrlCannotBeParsedThenDoNotThrowAnException() {
        val url = "://allowlist-tracker-1.com:5000/videos.js"
        givenDomainIsAnException("allowlist-tracker-1.com")

        assertFalse(testee.isAnException("test.com", url))
    }

    private fun givenDomainIsAnException(domain: String) {
        val exceptions = CopyOnWriteArrayList<TrackerAllowlistEntity>()
        val entity = TrackerAllowlistEntity(domain, emptyList())
        exceptions.add(entity)
        whenever(mockTrackerAllowlistRepository.exceptions).thenReturn(exceptions)
    }
}
