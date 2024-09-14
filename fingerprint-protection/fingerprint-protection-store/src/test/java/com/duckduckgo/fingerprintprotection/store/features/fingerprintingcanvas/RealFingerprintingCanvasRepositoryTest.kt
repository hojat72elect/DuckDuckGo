

package com.duckduckgo.fingerprintprotection.store.features.fingerprintingcanvas

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.fingerprintprotection.store.FingerprintProtectionDatabase
import com.duckduckgo.fingerprintprotection.store.FingerprintingCanvasEntity
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealFingerprintingCanvasRepositoryTest {
    @get:Rule var coroutineRule = CoroutineTestRule()

    private lateinit var testee: RealFingerprintingCanvasRepository

    private val mockDatabase: FingerprintProtectionDatabase = mock()
    private val mockFingerprintingCanvasDao: FingerprintingCanvasDao = mock()

    @Before
    fun before() {
        whenever(mockFingerprintingCanvasDao.get()).thenReturn(null)
        whenever(mockDatabase.fingerprintingCanvasDao()).thenReturn(mockFingerprintingCanvasDao)
    }

    @Test
    fun whenInitializedAndDoesNotHaveStoredValueThenLoadEmptyJsonToMemory() =
        runTest {
            testee =
                RealFingerprintingCanvasRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            verify(mockFingerprintingCanvasDao).get()
            assertEquals("{}", testee.fingerprintingCanvasEntity.json)
        }

    @Test
    fun whenInitializedAndHasStoredValueThenLoadStoredJsonToMemory() =
        runTest {
            whenever(mockFingerprintingCanvasDao.get()).thenReturn(fingerprintingCanvasEntity)
            testee =
                RealFingerprintingCanvasRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            verify(mockFingerprintingCanvasDao).get()
            assertEquals(fingerprintingCanvasEntity.json, testee.fingerprintingCanvasEntity.json)
        }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealFingerprintingCanvasRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    isMainProcess = true,
                )

            testee.updateAll(fingerprintingCanvasEntity)

            verify(mockFingerprintingCanvasDao).updateAll(fingerprintingCanvasEntity)
        }

    companion object {
        val fingerprintingCanvasEntity = FingerprintingCanvasEntity(json = "{\"key\":\"value\"}")
    }
}
