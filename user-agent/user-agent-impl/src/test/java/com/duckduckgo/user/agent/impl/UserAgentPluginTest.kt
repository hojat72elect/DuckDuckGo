package com.duckduckgo.user.agent.impl

import com.duckduckgo.common.test.FileUtilities
import com.duckduckgo.user.agent.store.UserAgentExceptionEntity
import com.duckduckgo.user.agent.store.UserAgentFeatureName
import com.duckduckgo.user.agent.store.UserAgentFeatureToggle
import com.duckduckgo.user.agent.store.UserAgentFeatureToggleRepository
import com.duckduckgo.user.agent.store.UserAgentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserAgentPluginTest {
    lateinit var testee: UserAgentPlugin

    private val mockFeatureTogglesRepository: UserAgentFeatureToggleRepository = mock()
    private val mockUserAgentRepository: UserAgentRepository = mock()

    @Before
    fun before() {
        testee = UserAgentPlugin(mockUserAgentRepository, mockFeatureTogglesRepository)
    }

    @Test
    fun whenFeatureNameDoesNotMatchUserAgentThenReturnFalse() {
        UserAgentFeatureName.values().filter { it != FEATURE_NAME }.forEach {
            assertFalse(testee.store(it.value, EMPTY_JSON_STRING))
        }
    }

    @Test
    fun whenFeatureNameMatchesUserAgentThenReturnTrue() {
        assertTrue(testee.store(FEATURE_NAME_VALUE, EMPTY_JSON_STRING))
    }

    @Test
    fun whenFeatureNameMatchesUserAgentAndIsEnabledThenStoreFeatureEnabled() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/useragent.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(
            UserAgentFeatureToggle(
                FEATURE_NAME_VALUE,
                true,
                null
            )
        )
    }

    @Test
    fun whenFeatureNameMatchesUserAgentAndIsNotEnabledThenStoreFeatureDisabled() {
        val jsonString =
            FileUtilities.loadText(javaClass.classLoader!!, "json/useragent_disabled.json")

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(
            UserAgentFeatureToggle(
                FEATURE_NAME_VALUE,
                false,
                null
            )
        )
    }

    @Test
    fun whenFeatureNameMatchesUserAgentAndHasMinSupportedVersionThenStoreMinSupportedVersion() {
        val jsonString = FileUtilities.loadText(
            javaClass.classLoader!!,
            "json/useragent_min_supported_version.json"
        )

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockFeatureTogglesRepository).insert(
            UserAgentFeatureToggle(
                FEATURE_NAME_VALUE,
                true,
                1234
            )
        )
    }

    @Test
    fun whenFeatureNameMatchesUserAgentThenUpdateAllExistingExceptions() {
        val jsonString = FileUtilities.loadText(javaClass.classLoader!!, "json/useragent.json")
        val exceptionsCaptor = argumentCaptor<List<UserAgentExceptionEntity>>()

        testee.store(FEATURE_NAME_VALUE, jsonString)

        verify(mockUserAgentRepository).updateAll(exceptionsCaptor.capture())

        assertEquals("example.com", exceptionsCaptor.firstValue[0].domain)
        assertEquals("foo.com", exceptionsCaptor.firstValue[1].domain)
        assertEquals("bar.com", exceptionsCaptor.firstValue[2].domain)
    }

    companion object {
        private val FEATURE_NAME = UserAgentFeatureName.UserAgent
        private val FEATURE_NAME_VALUE = FEATURE_NAME.value
        private const val EMPTY_JSON_STRING = "{}"
    }
}
