package com.duckduckgo.cookies.impl.thirdpartycookienames

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.cookies.api.ThirdPartyCookieNames
import com.duckduckgo.cookies.store.CookiesRepository
import java.util.concurrent.CopyOnWriteArrayList
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RealThirdPartyCookieNamesTest {
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    lateinit var testee: ThirdPartyCookieNames

    private val cookiesRepository: CookiesRepository = mock()

    @Before
    fun before() {
        testee = RealThirdPartyCookieNames(cookiesRepository)
    }

    @Test
    fun whenHasExcludedCookieNameCalledAndContainsCookieNameThenReturnTrue() {
        whenever(cookiesRepository.cookieNames).thenReturn(
            CopyOnWriteArrayList(
                listOf(
                    "anotherCookieName",
                    COOKIE_NAME
                )
            )
        )
        assertTrue(testee.hasExcludedCookieName(COOKIE_NAME))
    }

    @Test
    fun whenHasExcludedCookieNameCalledAndDoesNotContainCookieNameThenReturnFalse() {
        whenever(cookiesRepository.cookieNames).thenReturn(CopyOnWriteArrayList(listOf("anotherCookieName")))
        assertFalse(testee.hasExcludedCookieName(COOKIE_NAME))
    }

    companion object {
        private const val COOKIE_NAME = "cookieName"
    }
}
