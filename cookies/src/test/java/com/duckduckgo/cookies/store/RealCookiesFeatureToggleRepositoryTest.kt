package com.duckduckgo.cookies.store

import com.duckduckgo.cookies.api.CookiesFeatureName.Cookie
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RealCookiesFeatureToggleRepositoryTest {
    lateinit var testee: RealCookiesFeatureToggleRepository

    private val mockCookiesFeatureToggleStore: CookiesFeatureToggleStore =
        mock()

    @Before
    fun before() {
        testee = RealCookiesFeatureToggleRepository(
            mockCookiesFeatureToggleStore
        )
    }

    @Test
    fun whenDeleteAllThenDeleteAllCalled() {
        testee.deleteAll()

        verify(mockCookiesFeatureToggleStore).deleteAll()
    }

    @Test
    fun whenGetThenGetCalled() {
        testee.get(Cookie, true)

        verify(mockCookiesFeatureToggleStore).get(Cookie, true)
    }

    @Test
    fun whenInsertThenInsertCalled() {
        val cookieFeatureToggle =
            CookiesFeatureToggles(Cookie, true, null)
        testee.insert(cookieFeatureToggle)

        verify(mockCookiesFeatureToggleStore).insert(cookieFeatureToggle)
    }
}
