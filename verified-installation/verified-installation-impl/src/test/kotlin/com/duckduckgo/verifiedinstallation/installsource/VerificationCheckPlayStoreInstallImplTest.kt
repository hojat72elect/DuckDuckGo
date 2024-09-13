package com.duckduckgo.verifiedinstallation.installsource

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class VerificationCheckPlayStoreInstallImplTest {

    private val installSourceExtractor: InstallSourceExtractor = mock()
    private val testee = VerificationCheckPlayStoreInstallImpl(installSourceExtractor)

    @Test
    fun whenInstallSourceMatchesPlayStorePackageThenIdentifiedAsInstalledFromPlayStore() {
        whenever(installSourceExtractor.extract()).thenReturn("com.android.vending")
        assertTrue(testee.installedFromPlayStore())
    }

    @Test
    fun whenInstallSourceDoesNotMatchPlayStorePackageThenNotIdentifiedAsInstalledFromPlayStore() {
        whenever(installSourceExtractor.extract()).thenReturn("com.random.app")
        assertFalse(testee.installedFromPlayStore())
    }
}
