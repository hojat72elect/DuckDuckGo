package com.duckduckgo.elementhiding.store

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

class ElementHidingRepositoryTest {
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private lateinit var testee: RealElementHidingRepository

    private val mockDatabase: ElementHidingDatabase = mock()
    private val mockElementHidingDao: ElementHidingDao = mock()

    @Before
    fun before() {
        whenever(mockElementHidingDao.get()).thenReturn(null)
        whenever(mockDatabase.elementHidingDao()).thenReturn(mockElementHidingDao)
    }

    @Test
    fun whenInitializedAndDoesNotHaveStoredValueThenLoadEmptyJsonToMemory() =
        runTest {
            testee =
                RealElementHidingRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            verify(mockElementHidingDao).get()
            assertEquals("{}", testee.elementHidingEntity.json)
        }

    @Test
    fun whenInitializedAndHasStoredValueThenLoadStoredJsonToMemory() =
        runTest {
            whenever(mockElementHidingDao.get()).thenReturn(elementHidingEntity)
            testee =
                RealElementHidingRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            verify(mockElementHidingDao).get()
            assertEquals(elementHidingEntity.json, testee.elementHidingEntity.json)
        }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealElementHidingRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            testee.updateAll(elementHidingEntity)

            verify(mockElementHidingDao).updateAll(elementHidingEntity)
        }

    companion object {
        val elementHidingEntity = ElementHidingEntity(json = "{\"key\":\"value\"}")
    }
}
