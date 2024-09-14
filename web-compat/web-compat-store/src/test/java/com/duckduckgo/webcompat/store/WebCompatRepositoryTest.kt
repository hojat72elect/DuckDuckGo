

package com.duckduckgo.webcompat.store

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

class WebCompatRepositoryTest {
    @get:Rule var coroutineRule = CoroutineTestRule()

    private lateinit var testee: RealWebCompatRepository

    private val mockDatabase: WebCompatDatabase = mock()
    private val mockWebCompatDao: WebCompatDao = mock()

    @Before
    fun before() {
        whenever(mockWebCompatDao.get()).thenReturn(null)
        whenever(mockDatabase.webCompatDao()).thenReturn(mockWebCompatDao)
    }

    @Test
    fun whenInitializedAndDoesNotHaveStoredValueThenLoadEmptyJsonToMemory() =
        runTest {
            testee =
                RealWebCompatRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            verify(mockWebCompatDao).get()
            assertEquals("{}", testee.getWebCompatEntity().json)
        }

    @Test
    fun whenInitializedAndHasStoredValueThenLoadStoredJsonToMemory() =
        runTest {
            whenever(mockWebCompatDao.get()).thenReturn(webCompatEntity)
            testee =
                RealWebCompatRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            verify(mockWebCompatDao).get()
            assertEquals(webCompatEntity.json, testee.getWebCompatEntity().json)
        }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealWebCompatRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            testee.updateAll(webCompatEntity)

            verify(mockWebCompatDao).updateAll(webCompatEntity)
        }

    companion object {
        val webCompatEntity = WebCompatEntity(json = "{\"key\":\"value\"}")
    }
}
