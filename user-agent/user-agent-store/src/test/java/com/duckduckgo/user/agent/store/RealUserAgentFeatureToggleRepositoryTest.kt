package com.duckduckgo.user.agent.store

import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RealUserAgentFeatureToggleRepositoryTest {

    lateinit var testee: RealUserAgentFeatureToggleRepository

    private val mockPrivacyFeatureTogglesStore: UserAgentFeatureToggleStore = mock()

    @Before
    fun before() {
        testee = RealUserAgentFeatureToggleRepository(mockPrivacyFeatureTogglesStore)
    }

    @Test
    fun whenDeleteAllThenDeleteAllCalled() {
        testee.deleteAll()

        verify(mockPrivacyFeatureTogglesStore).deleteAll()
    }

    @Test
    fun whenGetThenGetCalled() {
        testee.get(UserAgentFeatureName.UserAgent, true)

        verify(mockPrivacyFeatureTogglesStore).get(UserAgentFeatureName.UserAgent, true)
    }

    @Test
    fun whenInsertThenInsertCalled() {
        val privacyFeatureToggle =
            UserAgentFeatureToggle(UserAgentFeatureName.UserAgent.value, true, null)
        testee.insert(privacyFeatureToggle)

        verify(mockPrivacyFeatureTogglesStore).insert(privacyFeatureToggle)
    }
}
