

package com.duckduckgo.request.filterer.store

import com.duckduckgo.request.filterer.api.RequestFiltererFeatureName.RequestFilterer
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RealFiltererRequestFeatureToggleRepositoryTest {
    lateinit var testee: RequestFiltererFeatureToggleRepository

    private val mockRequestFiltererFeatureToggleStore: RequestFiltererFeatureToggleRepository = mock()

    @Before
    fun before() {
        testee = RealRequestFiltererFeatureToggleRepository(mockRequestFiltererFeatureToggleStore)
    }

    @Test
    fun whenDeleteAllThenDeleteAllCalled() {
        testee.deleteAll()

        verify(mockRequestFiltererFeatureToggleStore).deleteAll()
    }

    @Test
    fun whenGetThenGetCalled() {
        testee.get(RequestFilterer, true)

        verify(mockRequestFiltererFeatureToggleStore).get(RequestFilterer, true)
    }

    @Test
    fun whenInsertThenInsertCalled() {
        val requestFiltererFeatureToggle = RequestFiltererFeatureToggles(RequestFilterer, true, null)
        testee.insert(requestFiltererFeatureToggle)

        verify(mockRequestFiltererFeatureToggleStore).insert(requestFiltererFeatureToggle)
    }
}
