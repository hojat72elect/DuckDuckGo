

package com.duckduckgo.fingerprintprotection.store.features.fingerprintinghardware

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.fingerprintprotection.store.FingerprintProtectionDatabase
import com.duckduckgo.fingerprintprotection.store.FingerprintingHardwareEntity
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealFingerprintingHardwareRepositoryTest {
    @get:Rule var coroutineRule = CoroutineTestRule()

    private lateinit var testee: RealFingerprintingHardwareRepository

    private val mockDatabase: FingerprintProtectionDatabase = mock()
    private val mockFingerprintingHardwareDao: FingerprintingHardwareDao = mock()

    @Before
    fun before() {
        whenever(mockFingerprintingHardwareDao.get()).thenReturn(null)
        whenever(mockDatabase.fingerprintingHardwareDao()).thenReturn(mockFingerprintingHardwareDao)
    }

    @Test
    fun whenInitializedAndDoesNotHaveStoredValueThenLoadEmptyJsonToMemory() =
        runTest {
            testee =
                RealFingerprintingHardwareRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            verify(mockFingerprintingHardwareDao).get()
            assertEquals("{}", testee.fingerprintingHardwareEntity.json)
        }

    @Test
    fun whenInitializedAndHasStoredValueThenLoadStoredJsonToMemory() =
        runTest {
            whenever(mockFingerprintingHardwareDao.get()).thenReturn(fingerprintingHardwareEntity)
            testee =
                RealFingerprintingHardwareRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            verify(mockFingerprintingHardwareDao).get()
            assertEquals(fingerprintingHardwareEntity.json, testee.fingerprintingHardwareEntity.json)
        }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealFingerprintingHardwareRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            testee.updateAll(fingerprintingHardwareEntity)

            verify(mockFingerprintingHardwareDao).updateAll(fingerprintingHardwareEntity)
        }

    companion object {
        val fingerprintingHardwareEntity = FingerprintingHardwareEntity(json = "{\"key\":\"value\"}")
    }
}
