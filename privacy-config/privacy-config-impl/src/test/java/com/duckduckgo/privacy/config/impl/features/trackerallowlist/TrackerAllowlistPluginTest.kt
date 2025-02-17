

package com.duckduckgo.privacy.config.impl.features.trackerallowlist

import com.duckduckgo.common.test.FileUtilities
import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import com.duckduckgo.privacy.config.store.PrivacyFeatureToggles
import com.duckduckgo.privacy.config.store.PrivacyFeatureTogglesRepository
import com.duckduckgo.privacy.config.store.TrackerAllowlistEntity
import com.duckduckgo.privacy.config.store.features.trackerallowlist.TrackerAllowlistRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TrackerAllowlistPluginTest {
    lateinit var testee: TrackerAllowlistPlugin

    private val mockFeatureTogglesRepository: PrivacyFeatureTogglesRepository = mock()
    private val mockAllowlistRepository: TrackerAllowlistRepository = mock()

    @Before
    fun before() {
        testee = TrackerAllowlistPlugin(mockAllowlistRepository, mockFeatureTogglesRepository)
    }

    @Test
    fun whenFeatureNameDoesNotMatchTrackerAllowlistThenReturnFalse() {
        PrivacyFeatureName.values().filter { it != FEATURE_NAME }.forEach {
            assertFalse(testee.store(it.value, EMPTY_JSON_STRING))
        }
    }

    @Test
    fun whenFeatureNameMatchesTrackerAllowlistThenReturnTrue() {
        assertTrue(testee.store(FEATURE_NAME_VALUE, EMPTY_JSON_STRING))
    }

    @Test
    fun whenFeatureNameMatchesTrackerAllowlistAndIsEnabledThenStoreFeatureEnabled() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/tracker_allowlist.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(PrivacyFeatureToggles(FEATURE_NAME_VALUE, true, null))
    }

    @Test
    fun whenFeatureNameMatchesTrackerAllowlistAndIsNotEnabledThenStoreFeatureDisabled() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/tracker_allowlist_disabled.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(PrivacyFeatureToggles(FEATURE_NAME_VALUE, false, null))
    }

    @Test
    fun whenFeatureNameMatchesTrackerAllowlistAndHasMinSupportedVersionThenStoreMinSupportedVersion() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/tracker_allowlist_min_supported_version.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(PrivacyFeatureToggles(FEATURE_NAME_VALUE, true, 1234))
    }

    @Test
    fun whenFeatureNameMatchesTrackerAllowlistThenUpdateAllExistingExceptions() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/tracker_allowlist.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        argumentCaptor<List<TrackerAllowlistEntity>>().apply {
            verify(mockAllowlistRepository).updateAll(capture())
            val trackerAllowlistEntity = this.firstValue.first()
            val rules = trackerAllowlistEntity.rules
            assertEquals(1, this.allValues.size)
            assertEquals("allowlist-tracker-1.com", trackerAllowlistEntity.domain)
            assertEquals("allowlist-tracker-1.com/videos.js", rules.first().rule)
            assertEquals("testsite.com", rules.first().domains.first())
            assertEquals("match single resource on single site", rules.first().reason)
        }
    }

    companion object {
        private val FEATURE_NAME = PrivacyFeatureName.TrackerAllowlistFeatureName
        private val FEATURE_NAME_VALUE = FEATURE_NAME.value
        private const val EMPTY_JSON_STRING = "{}"
    }
}
