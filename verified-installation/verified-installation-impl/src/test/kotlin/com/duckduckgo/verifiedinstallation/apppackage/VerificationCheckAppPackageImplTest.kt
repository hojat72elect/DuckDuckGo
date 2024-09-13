package com.duckduckgo.verifiedinstallation.apppackage

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class VerificationCheckAppPackageImplTest {

    private val appBuildConfig: AppBuildConfig = mock()
    private val testee = VerificationCheckAppPackageImpl(appBuildConfig)

    @Test
    fun whenPackageIsDebugVersionThenIsNotVerified() {
        whenever(appBuildConfig.applicationId).thenReturn("com.duckduckgo.mobile.android.debug")
        assertFalse(testee.isProductionPackage())
    }

    @Test
    fun whenPackageIsProductionVersionThenIsVerified() {
        whenever(appBuildConfig.applicationId).thenReturn("com.duckduckgo.mobile.android")
        assertTrue(testee.isProductionPackage())
    }

    @Test
    fun whenPackageIsUnrelatedToUsThenIsNotVerified() {
        whenever(appBuildConfig.applicationId).thenReturn("com.random.app")
        assertFalse(testee.isProductionPackage())
    }
}
