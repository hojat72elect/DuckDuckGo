package com.duckduckgo.autoconsent.impl.remoteconfig

import com.duckduckgo.autoconsent.impl.store.AutoconsentDao
import com.duckduckgo.autoconsent.impl.store.AutoconsentDatabase
import com.duckduckgo.autoconsent.impl.store.AutoconsentExceptionEntity
import com.duckduckgo.autoconsent.impl.store.toFeatureException
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

class RealAutoconsentExceptionsRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val mockDatabase: AutoconsentDatabase = mock()
    private val mockDao: AutoconsentDao = mock()

    lateinit var repository: AutoconsentExceptionsRepository

    @Before
    fun before() {
        whenever(mockDatabase.autoconsentDao()).thenReturn(mockDao)
    }

    @Test
    fun whenRepositoryIsCreatedThenExceptionsLoadedIntoMemory() {
        givenDaoContainsExceptions()

        repository = RealAutoconsentExceptionsRepository(
            TestScope(),
            coroutineRule.testDispatcherProvider,
            mockDatabase,
            isMainProcess = true
        )

        assertEquals(exception.toFeatureException(), repository.exceptions.first())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() = runTest {
        repository = RealAutoconsentExceptionsRepository(
            TestScope(),
            coroutineRule.testDispatcherProvider,
            mockDatabase,
            isMainProcess = true
        )

        repository.insertAllExceptions(listOf())

        verify(mockDao).updateAllExceptions(anyList())
    }

    @Test
    fun whenUpdateAllThenPreviousExceptionsAreCleared() = runTest {
        givenDaoContainsExceptions()
        repository = RealAutoconsentExceptionsRepository(
            TestScope(),
            coroutineRule.testDispatcherProvider,
            mockDatabase,
            isMainProcess = true
        )

        assertEquals(1, repository.exceptions.size)
        reset(mockDao)

        repository.insertAllExceptions(listOf())

        assertEquals(0, repository.exceptions.size)
    }

    private fun givenDaoContainsExceptions() {
        whenever(mockDao.getExceptions()).thenReturn(listOf(exception))
    }

    companion object {
        val exception = AutoconsentExceptionEntity("example.com", "reason")
    }
}
