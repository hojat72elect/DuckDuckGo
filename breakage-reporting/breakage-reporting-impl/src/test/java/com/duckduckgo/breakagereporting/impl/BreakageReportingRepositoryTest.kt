package com.duckduckgo.breakagereporting.impl

import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BreakageReportingRepositoryTest {
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private lateinit var testee: BreakageReportingRepository

    private val mockDatabase: BreakageReportingDatabase = mock()
    private val mockBreakageReportingDao: BreakageReportingDao = mock()

    @Before
    fun before() {
        whenever(mockBreakageReportingDao.get()).thenReturn(null)
        whenever(mockDatabase.breakageReportingDao()).thenReturn(mockBreakageReportingDao)
    }

    @Test
    fun whenInitializedAndDoesNotHaveStoredValueThenLoadEmptyJsonToMemory() =
        runTest {
            testee =
                RealBreakageReportingRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            verify(mockBreakageReportingDao).get()
            assertEquals("{}", testee.getBreakageReportingEntity().json)
        }

    @Test
    fun whenInitializedAndHasStoredValueThenLoadStoredJsonToMemory() =
        runTest {
            whenever(mockBreakageReportingDao.get()).thenReturn(breakageReportingEntity)
            testee =
                RealBreakageReportingRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            verify(mockBreakageReportingDao).get()
            assertEquals(breakageReportingEntity.json, testee.getBreakageReportingEntity().json)
        }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealBreakageReportingRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            testee.updateAll(breakageReportingEntity)

            verify(mockBreakageReportingDao).updateAll(breakageReportingEntity)
        }

    companion object {
        val breakageReportingEntity =
            BreakageReportingEntity(json = "{\"key\":\"value\"}")
    }
}
