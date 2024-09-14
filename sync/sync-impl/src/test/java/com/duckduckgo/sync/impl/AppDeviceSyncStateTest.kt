

package com.duckduckgo.sync.impl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class AppDeviceSyncStateTest {

    private val syncFeatureToggle: SyncFeatureToggle = mock()
    private val syncAccountRepository: SyncAccountRepository = mock()
    private val appDeviceSyncState = AppDeviceSyncState(syncFeatureToggle, syncAccountRepository)

    @Test
    fun whenUserSignedInThenDeviceSyncEnabled() {
        whenever(syncAccountRepository.isSignedIn()).thenReturn(true)

        assertTrue(appDeviceSyncState.isUserSignedInOnDevice())
    }

    @Test
    fun whenShowSyncDisabledThenFeatureDisabled() {
        givenFeatureFlag(enabled = false)

        assertFalse(appDeviceSyncState.isFeatureEnabled())
    }

    @Test
    fun whenShowSyncEnabledThenFeatureEnabled() {
        givenFeatureFlag(enabled = true)

        assertTrue(appDeviceSyncState.isFeatureEnabled())
    }

    private fun givenFeatureFlag(enabled: Boolean) {
        whenever(syncFeatureToggle.showSync()).thenReturn(enabled)
    }
}
