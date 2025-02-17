package com.duckduckgo.user.agent.store

import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealUserAgentRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    lateinit var testee: RealUserAgentRepository

    private val mockDatabase: UserAgentDatabase = mock()
    private val mockUserAgentExceptionsDao: UserAgentExceptionsDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.userAgentExceptionsDao()).thenReturn(mockUserAgentExceptionsDao)
        testee =
            RealUserAgentRepository(
                mockDatabase,
                TestScope(),
                coroutineRule.testDispatcherProvider,
                isMainProcess = true,
            )
    }

    @Test
    fun whenRepositoryIsCreatedThenExceptionsLoadedIntoMemory() {
        givenUserAgentExceptionsDaoContainExceptions()
        testee =
            RealUserAgentRepository(
                mockDatabase,
                TestScope(),
                coroutineRule.testDispatcherProvider,
                isMainProcess = true,
            )

        assertEquals(testee.exceptions.first(), exception.toFeatureException())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() = runTest {
        testee =
            RealUserAgentRepository(
                mockDatabase,
                TestScope(),
                coroutineRule.testDispatcherProvider,
                isMainProcess = true,
            )

        testee.updateAll(listOf())

        verify(mockUserAgentExceptionsDao).updateAll(anyList())
    }

    @Test
    fun whenUpdateAllThenPreviousExceptionsAreCleared() = runTest {
        givenUserAgentExceptionsDaoContainExceptions()
        testee =
            RealUserAgentRepository(
                mockDatabase,
                TestScope(),
                coroutineRule.testDispatcherProvider,
                isMainProcess = true,
            )
        assertEquals(1, testee.exceptions.size)
        reset(mockUserAgentExceptionsDao)

        testee.updateAll(listOf())

        assertEquals(0, testee.exceptions.size)
    }

    private fun givenUserAgentExceptionsDaoContainExceptions() {
        whenever(mockUserAgentExceptionsDao.getAll()).thenReturn(listOf(exception))
    }

    companion object {
        val exception = UserAgentExceptionEntity("example.com", "reason")
    }
}
