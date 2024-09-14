

package com.duckduckgo.fingerprintprotection.store.seed

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class RealFingerprintProtectionSeedRepositoryTest {
    private lateinit var testee: RealFingerprintProtectionSeedRepository

    @Before
    fun before() {
        testee = RealFingerprintProtectionSeedRepository()
    }

    @Test
    fun whenInitializedRandomSeedIsSet() {
        assertFalse(testee.seed.isBlank())
    }

    @Test
    fun whenStoredNewSeedThenSeedIsRefreshed() {
        val oldSeed = testee.seed
        testee.storeNewSeed()
        assertNotEquals(oldSeed, testee.seed)
    }
}
