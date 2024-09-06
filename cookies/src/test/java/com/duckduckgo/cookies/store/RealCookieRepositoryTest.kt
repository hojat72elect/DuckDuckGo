/*
 * Copyright (c) 2024 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.cookies.store

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.cookies.store.RealCookieRepository.Companion.DEFAULT_MAX_AGE
import com.duckduckgo.cookies.store.RealCookieRepository.Companion.DEFAULT_THRESHOLD
import com.duckduckgo.cookies.store.thirdpartycookienames.ThirdPartyCookieNamesDao
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealCookieRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    lateinit var testee: com.duckduckgo.cookies.store.RealCookieRepository

    private val mockDatabase: com.duckduckgo.cookies.store.CookiesDatabase = mock()
    private val mockCookiesDao: com.duckduckgo.cookies.store.CookiesDao = mock()
    private val mockCookieNamesDao: ThirdPartyCookieNamesDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.cookiesDao()).thenReturn(mockCookiesDao)
        whenever(mockDatabase.cookieNamesDao()).thenReturn(mockCookieNamesDao)
    }

    @Test
    fun whenRepositoryIsCreatedThenValuesLoadedIntoMemory() {
        givenCookiesDbHasContent()

        testee = com.duckduckgo.cookies.store.RealCookieRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            true,
        )

        assertEquals(cookieExceptionEntity.toFeatureException(), testee.exceptions.first())
        assertEquals(THRESHOLD, testee.firstPartyCookiePolicy.threshold)
        assertEquals(MAX_AGE, testee.firstPartyCookiePolicy.maxAge)
        assertEquals(cookieNamesEntity.name, testee.cookieNames.first())
    }

    @Test
    fun whenLoadToMemoryAndNoPolicyThenSetDefaultValues() {
        whenever(mockCookiesDao.getFirstPartyCookiePolicy()).thenReturn(null)

        testee = com.duckduckgo.cookies.store.RealCookieRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            true,
        )

        assertEquals(DEFAULT_THRESHOLD, testee.firstPartyCookiePolicy.threshold)
        assertEquals(DEFAULT_MAX_AGE, testee.firstPartyCookiePolicy.maxAge)
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() {
        val policy = com.duckduckgo.cookies.store.FirstPartyCookiePolicyEntity(5, 6, 7)

        testee = com.duckduckgo.cookies.store.RealCookieRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            true,
        )

        testee.updateAll(listOf(), policy, listOf())

        verify(mockCookiesDao).updateAll(emptyList(), policy)
        verify(mockCookieNamesDao).updateAllCookieNames(emptyList())
    }

    @Test
    fun whenUpdateAllThenPreviousValuesAreCleared() {
        givenCookiesDbHasContent()

        testee = com.duckduckgo.cookies.store.RealCookieRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            true,
        )
        assertEquals(1, testee.exceptions.size)
        assertEquals(THRESHOLD, testee.firstPartyCookiePolicy.threshold)
        assertEquals(MAX_AGE, testee.firstPartyCookiePolicy.maxAge)
        assertEquals(1, testee.cookieNames.size)

        reset(mockCookiesDao)
        reset(mockCookieNamesDao)

        testee.updateAll(listOf(),
            com.duckduckgo.cookies.store.FirstPartyCookiePolicyEntity(5, 6, 7), listOf())

        assertEquals(0, testee.exceptions.size)
        assertEquals(0, testee.cookieNames.size)
    }

    private fun givenCookiesDbHasContent() {
        whenever(mockCookiesDao.getAllCookieExceptions()).thenReturn(listOf(cookieExceptionEntity))
        whenever(mockCookiesDao.getFirstPartyCookiePolicy()).thenReturn(
            com.duckduckgo.cookies.store.FirstPartyCookiePolicyEntity(
                1,
                THRESHOLD,
                MAX_AGE
            )
        )
        whenever(mockCookieNamesDao.getCookieNames()).thenReturn(listOf(cookieNamesEntity))
    }

    companion object {
        val cookieExceptionEntity = com.duckduckgo.cookies.store.CookieExceptionEntity(
            domain = "https://www.example.com",
            reason = "reason",
        )

        val cookieNamesEntity = com.duckduckgo.cookies.store.CookieNamesEntity(
            name = "cookieName",
        )

        const val THRESHOLD = 2
        const val MAX_AGE = 3
    }
}
