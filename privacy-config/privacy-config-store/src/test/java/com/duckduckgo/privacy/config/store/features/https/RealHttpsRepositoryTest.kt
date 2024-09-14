

package com.duckduckgo.privacy.config.store.features.https

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.privacy.config.store.HttpsExceptionEntity
import com.duckduckgo.privacy.config.store.PrivacyConfigDatabase
import com.duckduckgo.privacy.config.store.toFeatureException
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealHttpsRepositoryTest {

    @get:Rule var coroutineRule = CoroutineTestRule()

    lateinit var testee: RealHttpsRepository

    private val mockDatabase: PrivacyConfigDatabase = mock()
    private val mockHttpsDao: HttpsDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.httpsDao()).thenReturn(mockHttpsDao)
        testee =
            RealHttpsRepository(
                mockDatabase,
                TestScope(),
                coroutineRule.testDispatcherProvider,
                true,
            )
    }

    @Test
    fun whenRepositoryIsCreatedThenExceptionsLoadedIntoMemory() {
        givenHttpsDaoContainsExceptions()

        testee =
            RealHttpsRepository(
                mockDatabase,
                TestScope(),
                coroutineRule.testDispatcherProvider,
                true,
            )

        assertEquals(httpException.toFeatureException(), testee.exceptions.first())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealHttpsRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            testee.updateAll(listOf())

            verify(mockHttpsDao).updateAll(ArgumentMatchers.anyList())
        }

    @Test
    fun whenUpdateAllThenPreviousExceptionsAreCleared() =
        runTest {
            givenHttpsDaoContainsExceptions()
            testee =
                RealHttpsRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )
            assertEquals(1, testee.exceptions.size)
            reset(mockHttpsDao)

            testee.updateAll(listOf())

            assertEquals(0, testee.exceptions.size)
        }

    private fun givenHttpsDaoContainsExceptions() {
        whenever(mockHttpsDao.getAll()).thenReturn(listOf(httpException))
    }

    companion object {
        val httpException = HttpsExceptionEntity("example.com", "reason")
    }
}
