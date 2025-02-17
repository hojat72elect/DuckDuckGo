

package com.duckduckgo.fingerprintprotection.store.features.fingerprintingbattery

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.fingerprintprotection.store.FingerprintProtectionDatabase
import com.duckduckgo.fingerprintprotection.store.FingerprintingBatteryEntity
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealFingerprintingBatteryRepositoryTest {
    @get:Rule var coroutineRule = CoroutineTestRule()

    private lateinit var testee: RealFingerprintingBatteryRepository

    private val mockDatabase: FingerprintProtectionDatabase = mock()
    private val mockFingerprintingBatteryDao: FingerprintingBatteryDao = mock()

    @Before
    fun before() {
        whenever(mockFingerprintingBatteryDao.get()).thenReturn(null)
        whenever(mockDatabase.fingerprintingBatteryDao()).thenReturn(mockFingerprintingBatteryDao)
    }

    @Test
    fun whenInitializedAndDoesNotHaveStoredValueThenLoadEmptyJsonToMemory() =
        runTest {
            testee =
                RealFingerprintingBatteryRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            verify(mockFingerprintingBatteryDao).get()
            assertEquals("{}", testee.fingerprintingBatteryEntity.json)
        }

    @Test
    fun whenInitializedAndHasStoredValueThenLoadStoredJsonToMemory() =
        runTest {
            whenever(mockFingerprintingBatteryDao.get()).thenReturn(fingerprintingBatteryEntity)
            testee =
                RealFingerprintingBatteryRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            verify(mockFingerprintingBatteryDao).get()
            assertEquals(fingerprintingBatteryEntity.json, testee.fingerprintingBatteryEntity.json)
        }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealFingerprintingBatteryRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            testee.updateAll(fingerprintingBatteryEntity)

            verify(mockFingerprintingBatteryDao).updateAll(fingerprintingBatteryEntity)
        }

    companion object {
        val fingerprintingBatteryEntity = FingerprintingBatteryEntity(json = "{\"key\":\"value\"}")
    }
}
