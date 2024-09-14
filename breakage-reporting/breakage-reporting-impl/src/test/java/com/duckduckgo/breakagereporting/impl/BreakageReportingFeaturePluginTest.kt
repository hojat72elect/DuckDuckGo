

package com.duckduckgo.breakagereporting.impl

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class BreakageReportingFeaturePluginTest {
    lateinit var testee: BreakageReportingFeaturePlugin

    private val mockBreakageReportingRepository: BreakageReportingRepository = mock()

    @Before
    fun before() {
        testee = BreakageReportingFeaturePlugin(mockBreakageReportingRepository)
    }

    @Test
    fun whenFeatureNameDoesNotMatchBreakageReportingThenReturnFalse() {
        BreakageReportingFeatureName.values().filter { it != FEATURE_NAME }.forEach {
            assertFalse(testee.store(it.value, JSON_STRING))
        }
    }

    @Test
    fun whenFeatureNameMatchesBreakageReportingThenReturnTrue() {
        assertTrue(testee.store(FEATURE_NAME_VALUE, JSON_STRING))
    }

    @Test
    fun whenFeatureNameMatchesBreakageReportingThenUpdateAll() {
        testee.store(FEATURE_NAME_VALUE, JSON_STRING)
        val captor = argumentCaptor<BreakageReportingEntity>()
        verify(mockBreakageReportingRepository).updateAll(captor.capture())
        assertEquals(JSON_STRING, captor.firstValue.json)
    }

    companion object {
        private val FEATURE_NAME = BreakageReportingFeatureName.BreakageReporting
        private val FEATURE_NAME_VALUE = FEATURE_NAME.value
        private const val JSON_STRING = "{\"key\":\"value\"}"
    }
}
