

package com.duckduckgo.fingerprintprotection

import androidx.lifecycle.LifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.WorkManager
import com.duckduckgo.fingerprintprotection.impl.FingerprintProtectionSeedWorkerScheduler
import com.duckduckgo.fingerprintprotection.store.seed.FingerprintProtectionSeedRepository
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class FingerprintProtectionSeedWorkerSchedulerTest {

    private val fingerprintProtectionSeedRepository: FingerprintProtectionSeedRepository = mock()
    private val mockWorkManager: WorkManager = mock()
    private val mockOwner: LifecycleOwner = mock()

    lateinit var fingerprintProtectionSeedWorkerScheduler: FingerprintProtectionSeedWorkerScheduler

    @Before
    fun before() {
        fingerprintProtectionSeedWorkerScheduler = FingerprintProtectionSeedWorkerScheduler(mockWorkManager, fingerprintProtectionSeedRepository)
    }

    @Test
    fun whenOnStopThenStoreNewSeedAndEnqueueWorkWithReplacePolicy() {
        fingerprintProtectionSeedWorkerScheduler.onStop(mockOwner)

        verify(mockWorkManager).enqueueUniquePeriodicWork(any(), eq(REPLACE), any())
        verify(fingerprintProtectionSeedRepository).storeNewSeed()
    }

    @Test
    fun whenOnStartThenEnqueueWorkWithKeepPolicy() {
        fingerprintProtectionSeedWorkerScheduler.onStart(mockOwner)

        verify(mockWorkManager).enqueueUniquePeriodicWork(any(), eq(KEEP), any())
    }
}
