

package com.duckduckgo.privacy.config.store.features.drm

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.privacy.config.store.DrmExceptionEntity
import com.duckduckgo.privacy.config.store.PrivacyConfigDatabase
import com.duckduckgo.privacy.config.store.toFeatureException
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

class RealDrmRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    lateinit var testee: RealDrmRepository

    private val mockDatabase: PrivacyConfigDatabase = mock()
    private val mockDrmDao: DrmDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.drmDao()).thenReturn(mockDrmDao)
    }

    @Test
    fun whenRepositoryIsCreatedThenExceptionsLoadedIntoMemory() = runTest {
        givenDrmDaoContainsExceptions()

        testee = RealDrmRepository(mockDatabase, this, coroutineRule.testDispatcherProvider, true)

        assertEquals(drmException.toFeatureException(), testee.exceptions.first())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() = runTest {
        testee = RealDrmRepository(mockDatabase, this, coroutineRule.testDispatcherProvider, true)

        testee.updateAll(listOf())

        verify(mockDrmDao).updateAll(anyList())
    }

    @Test
    fun whenUpdateAllThenPreviousExceptionsAreCleared() = runTest {
        givenDrmDaoContainsExceptions()
        testee = RealDrmRepository(mockDatabase, this, coroutineRule.testDispatcherProvider, true)
        assertEquals(1, testee.exceptions.size)
        reset(mockDrmDao)

        testee.updateAll(listOf())

        assertEquals(0, testee.exceptions.size)
    }

    private fun givenDrmDaoContainsExceptions() {
        whenever(mockDrmDao.getAll()).thenReturn(listOf(drmException))
    }

    companion object {
        val drmException = DrmExceptionEntity("example.com", "reason")
    }
}
