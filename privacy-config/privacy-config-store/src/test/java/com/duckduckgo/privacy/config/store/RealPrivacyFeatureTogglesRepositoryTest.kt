

package com.duckduckgo.privacy.config.store

import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RealPrivacyFeatureTogglesRepositoryTest {

    lateinit var testee: RealPrivacyFeatureTogglesRepository

    private val mockPrivacyFeatureTogglesDataStore: PrivacyFeatureTogglesDataStore = mock()

    @Before
    fun before() {
        testee = RealPrivacyFeatureTogglesRepository(mockPrivacyFeatureTogglesDataStore)
    }

    @Test
    fun whenDeleteAllThenDeleteAllCalled() {
        testee.deleteAll()

        verify(mockPrivacyFeatureTogglesDataStore).deleteAll()
    }

    @Test
    fun whenGetThenGetCalled() {
        testee.get(PrivacyFeatureName.GpcFeatureName, true)

        verify(mockPrivacyFeatureTogglesDataStore).get(PrivacyFeatureName.GpcFeatureName, true)
    }

    @Test
    fun whenInsertThenInsertCalled() {
        val privacyFeatureToggle = PrivacyFeatureToggles(PrivacyFeatureName.GpcFeatureName.value, true, null)
        testee.insert(privacyFeatureToggle)

        verify(mockPrivacyFeatureTogglesDataStore).insert(privacyFeatureToggle)
    }
}
