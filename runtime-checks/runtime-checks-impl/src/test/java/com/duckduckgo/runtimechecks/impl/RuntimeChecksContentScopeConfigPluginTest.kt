

package com.duckduckgo.runtimechecks.impl

import com.duckduckgo.runtimechecks.store.RuntimeChecksEntity
import com.duckduckgo.runtimechecks.store.RuntimeChecksRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RuntimeChecksContentScopeConfigPluginTest {

    lateinit var testee: RuntimeChecksContentScopeConfigPlugin

    private val mockRuntimeChecksRepository: RuntimeChecksRepository = mock()

    @Before
    fun before() {
        testee = RuntimeChecksContentScopeConfigPlugin(mockRuntimeChecksRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockRuntimeChecksRepository.getRuntimeChecksEntity()).thenReturn(RuntimeChecksEntity(json = config))
        assertEquals("\"runtimeChecks\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
