package com.duckduckgo.elementhiding.impl

import com.duckduckgo.elementhiding.store.ElementHidingEntity
import com.duckduckgo.elementhiding.store.ElementHidingRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ElementHidingContentScopeConfigPluginTest {

    lateinit var testee: ElementHidingContentScopeConfigPlugin

    private val mockElementHidingRepository: ElementHidingRepository = mock()

    @Before
    fun before() {
        testee = ElementHidingContentScopeConfigPlugin(mockElementHidingRepository)
    }

    @Test
    fun whenGetConfigThenReturnCorrectlyFormattedJson() {
        whenever(mockElementHidingRepository.elementHidingEntity).thenReturn(
            ElementHidingEntity(
                json = config
            )
        )
        assertEquals("\"elementHiding\":$config", testee.config())
    }

    @Test
    fun whenGetPreferencesThenReturnNull() {
        assertNull(testee.preferences())
    }

    companion object {
        const val config = "{\"key\":\"value\"}"
    }
}
