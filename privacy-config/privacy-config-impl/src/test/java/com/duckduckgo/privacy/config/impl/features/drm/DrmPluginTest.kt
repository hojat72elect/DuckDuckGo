

package com.duckduckgo.privacy.config.impl.features.drm

import com.duckduckgo.common.test.FileUtilities
import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import com.duckduckgo.privacy.config.store.PrivacyFeatureToggles
import com.duckduckgo.privacy.config.store.PrivacyFeatureTogglesRepository
import com.duckduckgo.privacy.config.store.features.drm.DrmRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DrmPluginTest {
    lateinit var testee: DrmPlugin

    private val mockFeatureTogglesRepository: PrivacyFeatureTogglesRepository = mock()
    private val mockDrmRepository: DrmRepository = mock()

    @Before
    fun before() {
        testee = DrmPlugin(mockDrmRepository, mockFeatureTogglesRepository)
    }

    @Test
    fun whenFeatureNameDoesNotMatchEmeThenReturnFalse() {
        PrivacyFeatureName.values().filter { it != FEATURE_NAME }.forEach {
            assertFalse(testee.store(it.value, EMPTY_JSON_STRING))
        }
    }

    @Test
    fun whenFeatureNameMatchesEmeThenReturnTrue() {
        assertTrue(testee.store(FEATURE_NAME_VALUE, EMPTY_JSON_STRING))
    }

    @Test
    fun whenFeatureNameMatchesEmeAndIsEnabledThenStoreFeatureEnabled() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/drm.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(PrivacyFeatureToggles(FEATURE_NAME_VALUE, true, null))
    }

    @Test
    fun whenFeatureNameMatchesEmeAndIsNotEnabledThenStoreFeatureDisabled() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/drm_disabled.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(PrivacyFeatureToggles(FEATURE_NAME_VALUE, false, null))
    }

    @Test
    fun whenFeatureNameMatchesEmeAndHasMinSupportedVersionThenStoreMinSupportedVersion() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/drm_min_supported_version.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(PrivacyFeatureToggles(FEATURE_NAME_VALUE, true, 1234))
    }

    @Test
    fun whenFeatureNameMatchesEmeThenUpdateAllExistingExceptions() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/drm.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockDrmRepository).updateAll(anyList())
    }

    companion object {
        private val FEATURE_NAME = PrivacyFeatureName.DrmFeatureName
        private val FEATURE_NAME_VALUE = FEATURE_NAME.value
        private const val EMPTY_JSON_STRING = "{}"
    }
}
