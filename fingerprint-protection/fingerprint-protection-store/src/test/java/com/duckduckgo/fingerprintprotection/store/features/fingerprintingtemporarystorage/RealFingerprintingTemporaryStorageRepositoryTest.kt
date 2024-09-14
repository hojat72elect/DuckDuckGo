

package com.duckduckgo.fingerprintprotection.store.features.fingerprintingtemporarystorage

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.fingerprintprotection.store.FingerprintProtectionDatabase
import com.duckduckgo.fingerprintprotection.store.FingerprintingTemporaryStorageEntity
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealFingerprintingTemporaryStorageRepositoryTest {
    @get:Rule var coroutineRule = CoroutineTestRule()

    private lateinit var testee: RealFingerprintingTemporaryStorageRepository

    private val mockDatabase: FingerprintProtectionDatabase = mock()
    private val mockFingerprintingTemporaryStorageDao: FingerprintingTemporaryStorageDao = mock()

    @Before
    fun before() {
        whenever(mockFingerprintingTemporaryStorageDao.get()).thenReturn(null)
        whenever(mockDatabase.fingerprintingTemporaryStorageDao()).thenReturn(mockFingerprintingTemporaryStorageDao)
    }

    @Test
    fun whenInitializedAndDoesNotHaveStoredValueThenLoadEmptyJsonToMemory() =
        runTest {
            testee =
                RealFingerprintingTemporaryStorageRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            verify(mockFingerprintingTemporaryStorageDao).get()
            assertEquals("{}", testee.fingerprintingTemporaryStorageEntity.json)
        }

    @Test
    fun whenInitializedAndHasStoredValueThenLoadStoredJsonToMemory() =
        runTest {
            whenever(mockFingerprintingTemporaryStorageDao.get()).thenReturn(fingerprintingTemporaryStorageEntity)
            testee =
                RealFingerprintingTemporaryStorageRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            verify(mockFingerprintingTemporaryStorageDao).get()
            assertEquals(fingerprintingTemporaryStorageEntity.json, testee.fingerprintingTemporaryStorageEntity.json)
        }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee =
                RealFingerprintingTemporaryStorageRepository(
                    mockDatabase,
                    TestScope(),
                    coroutineRule.testDispatcherProvider,
                    true,
                )

            testee.updateAll(fingerprintingTemporaryStorageEntity)

            verify(mockFingerprintingTemporaryStorageDao).updateAll(fingerprintingTemporaryStorageEntity)
        }

    companion object {
        val fingerprintingTemporaryStorageEntity = FingerprintingTemporaryStorageEntity(json = "{\"key\":\"value\"}")
    }
}
