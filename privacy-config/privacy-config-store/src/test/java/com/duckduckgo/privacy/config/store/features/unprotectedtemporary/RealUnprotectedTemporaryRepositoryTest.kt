

package com.duckduckgo.privacy.config.store.features.unprotectedtemporary

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.privacy.config.store.PrivacyConfigDatabase
import com.duckduckgo.privacy.config.store.UnprotectedTemporaryEntity
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

class RealUnprotectedTemporaryRepositoryTest {

    @get:Rule var coroutineRule = CoroutineTestRule()

    lateinit var testee: RealUnprotectedTemporaryRepository

    private val mockDatabase: PrivacyConfigDatabase = mock()
    private val mockUnprotectedTemporaryDao: UnprotectedTemporaryDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.unprotectedTemporaryDao()).thenReturn(mockUnprotectedTemporaryDao)
        testee =
            RealUnprotectedTemporaryRepository(
                mockDatabase,
                TestScope(),
                coroutineRule.testDispatcherProvider,
                isMainProcess = true,
            )
    }

    @Test
    fun whenRepositoryIsCreatedThenExceptionsLoadedIntoMemory() {
        givenUnprotectedTemporaryDaoContainsExceptions()

        testee =
            RealUnprotectedTemporaryRepository(
                mockDatabase,
                TestScope(),
                coroutineRule.testDispatcherProvider,
                isMainProcess = true,
            )

        assertEquals(unprotectedTemporaryException.toFeatureException(), testee.exceptions.first())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealUnprotectedTemporaryRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            testee.updateAll(listOf())

            verify(mockUnprotectedTemporaryDao).updateAll(ArgumentMatchers.anyList())
        }

    @Test
    fun whenUpdateAllThenPreviousExceptionsAreCleared() =
        runTest {
            givenUnprotectedTemporaryDaoContainsExceptions()
            testee =
                RealUnprotectedTemporaryRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )
            assertEquals(1, testee.exceptions.size)
            reset(mockUnprotectedTemporaryDao)

            testee.updateAll(listOf())

            assertEquals(0, testee.exceptions.size)
        }

    private fun givenUnprotectedTemporaryDaoContainsExceptions() {
        whenever(mockUnprotectedTemporaryDao.getAll())
            .thenReturn(listOf(unprotectedTemporaryException))
    }

    companion object {
        val unprotectedTemporaryException = UnprotectedTemporaryEntity("example.com", "reason")
    }
}
