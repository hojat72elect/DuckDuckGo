

package com.duckduckgo.privacy.config.store.features.trackingparameters

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.privacy.config.store.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealTrackingParametersRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    lateinit var testee: RealTrackingParametersRepository

    private val mockDatabase: PrivacyConfigDatabase = mock()
    private val mockTrackingParametersDao: TrackingParametersDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.trackingParametersDao()).thenReturn(mockTrackingParametersDao)
        testee = RealTrackingParametersRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            isMainProcess = true,
        )
    }

    @Test
    fun whenRepositoryIsCreatedThenValuesLoadedIntoMemory() {
        givenTrackingParametersDaoContainsEntities()

        testee = RealTrackingParametersRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            isMainProcess = true,
        )

        assertEquals(trackingParameterExceptionEntity.toFeatureException(), testee.exceptions.first())
        assertEquals(trackingParameterEntity.parameter, testee.parameters.first().toString())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() = runTest {
        testee = RealTrackingParametersRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            isMainProcess = true,
        )

        testee.updateAll(listOf(), listOf())

        verify(mockTrackingParametersDao).updateAll(anyList(), anyList())
    }

    @Test
    fun whenUpdateAllThenPreviousValuesAreCleared() = runTest {
        givenTrackingParametersDaoContainsEntities()

        testee = RealTrackingParametersRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            isMainProcess = true,
        )
        assertEquals(1, testee.exceptions.size)
        assertEquals(1, testee.parameters.size)

        reset(mockTrackingParametersDao)

        testee.updateAll(listOf(), listOf())

        assertEquals(0, testee.exceptions.size)
        assertEquals(0, testee.parameters.size)
    }

    private fun givenTrackingParametersDaoContainsEntities() {
        whenever(mockTrackingParametersDao.getAllExceptions()).thenReturn(listOf(trackingParameterExceptionEntity))
        whenever(mockTrackingParametersDao.getAllTrackingParameters()).thenReturn(listOf(trackingParameterEntity))
    }

    companion object {
        val trackingParameterExceptionEntity = TrackingParameterExceptionEntity(
            domain = "https://www.example.com",
            reason = "reason",
        )

        val trackingParameterEntity = TrackingParameterEntity(
            parameter = "parameter",
        )
    }
}
