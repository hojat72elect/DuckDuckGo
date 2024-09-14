


package com.duckduckgo.breakagereporting.impl

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BreakageReportingContentScopeConfigPluginTest {

    lateinit var testee: BreakageReportingContentScopeConfigPlugin

    private val mockBreakageReportingRepository: BreakageReportingRepository = mock()

    @Before
    fun before() {
        testee = BreakageReportingContentScopeConfigPlugin(mockBreakageReportingRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockBreakageReportingRepository.getBreakageReportingEntity()).thenReturn(
            BreakageReportingEntity(json = config),
        )
        assertEquals("\"breakageReporting\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
