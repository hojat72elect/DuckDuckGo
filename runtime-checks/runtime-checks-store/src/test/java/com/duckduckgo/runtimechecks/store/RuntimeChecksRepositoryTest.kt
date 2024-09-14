

package com.duckduckgo.runtimechecks.store

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

class RuntimeChecksRepositoryTest {
    @get:Rule var coroutineRule = CoroutineTestRule()

    private lateinit var testee: RealRuntimeChecksRepository

    private val mockDatabase: RuntimeChecksDatabase = mock()
    private val mockRuntimeChecksDao: RuntimeChecksDao = mock()

    @Before
    fun before() {
        whenever(mockRuntimeChecksDao.get()).thenReturn(null)
        whenever(mockDatabase.runtimeChecksDao()).thenReturn(mockRuntimeChecksDao)
    }

    @Test
    fun whenInitializedAndDoesNotHaveStoredValueThenLoadEmptyJsonToMemory() =
        runTest {
            testee =
                RealRuntimeChecksRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            verify(mockRuntimeChecksDao).get()
            assertEquals("{}", testee.getRuntimeChecksEntity().json)
        }

    @Test
    fun whenInitializedAndHasStoredValueThenLoadStoredJsonToMemory() =
        runTest {
            whenever(mockRuntimeChecksDao.get()).thenReturn(runtimeChecksEntity)
            testee =
                RealRuntimeChecksRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            verify(mockRuntimeChecksDao).get()
            assertEquals(runtimeChecksEntity.json, testee.getRuntimeChecksEntity().json)
        }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealRuntimeChecksRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            testee.updateAll(runtimeChecksEntity)

            verify(mockRuntimeChecksDao).updateAll(runtimeChecksEntity)
        }

    companion object {
        val runtimeChecksEntity = RuntimeChecksEntity(json = "{\"key\":\"value\"}")
    }
}
