package com.duckduckgo.elementhiding.impl

import com.duckduckgo.elementhiding.store.ElementHidingEntity
import com.duckduckgo.elementhiding.store.ElementHidingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ElementHidingFeaturePluginTest {
    lateinit var testee: ElementHidingFeaturePlugin

    private val mockElementHidingRepository: ElementHidingRepository = mock()

    @Before
    fun before() {
        testee = ElementHidingFeaturePlugin(mockElementHidingRepository)
    }

    @Test
    fun whenFeatureNameDoesNotMatchElementHidingThenReturnFalse() {
        ElementHidingFeatureName.values().filter { it != FEATURE_NAME }.forEach {
            assertFalse(testee.store(it.value, JSON_STRING))
        }
    }

    @Test
    fun whenFeatureNameMatchesElementHidingThenReturnTrue() {
        assertTrue(testee.store(FEATURE_NAME_VALUE, JSON_STRING))
    }

    @Test
    fun whenFeatureNameMatchesElementHidingThenUpdateAll() {
        testee.store(FEATURE_NAME_VALUE, JSON_STRING)
        val captor = argumentCaptor<ElementHidingEntity>()
        verify(mockElementHidingRepository).updateAll(captor.capture())
        assertEquals(JSON_STRING, captor.firstValue.json)
    }

    companion object {
        private val FEATURE_NAME = ElementHidingFeatureName.ElementHiding
        private val FEATURE_NAME_VALUE = FEATURE_NAME.value
        private const val JSON_STRING = "{\"key\":\"value\"}"
    }
}
