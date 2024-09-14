

package com.duckduckgo.runtimechecks.impl

import com.duckduckgo.runtimechecks.store.RuntimeChecksEntity
import com.duckduckgo.runtimechecks.store.RuntimeChecksRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RuntimeChecksFeaturePluginTest {
    lateinit var testee: RuntimeChecksFeaturePlugin

    private val mockRuntimeChecksRepository: RuntimeChecksRepository = mock()

    @Before
    fun before() {
        testee = RuntimeChecksFeaturePlugin(mockRuntimeChecksRepository)
    }

    @Test
    fun whenFeatureNameDoesNotMatchRuntimeChecksThenReturnFalse() {
        RuntimeChecksFeatureName.values().filter { it != FEATURE_NAME }.forEach {
            assertFalse(testee.store(it.value, JSON_STRING))
        }
    }

    @Test
    fun whenFeatureNameMatchesRuntimeChecksThenReturnTrue() {
        assertTrue(testee.store(FEATURE_NAME_VALUE, JSON_STRING))
    }

    @Test
    fun whenFeatureNameMatchesRuntimeChecksThenUpdateAll() {
        testee.store(FEATURE_NAME_VALUE, JSON_STRING)
        val captor = argumentCaptor<RuntimeChecksEntity>()
        verify(mockRuntimeChecksRepository).updateAll(captor.capture())
        assertEquals(JSON_STRING, captor.firstValue.json)
    }

    companion object {
        private val FEATURE_NAME = RuntimeChecksFeatureName.RuntimeChecks
        private val FEATURE_NAME_VALUE = FEATURE_NAME.value
        private const val JSON_STRING = "{\"key\":\"value\"}"
    }
}
