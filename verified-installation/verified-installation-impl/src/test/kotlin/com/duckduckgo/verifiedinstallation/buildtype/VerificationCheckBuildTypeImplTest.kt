package com.duckduckgo.verifiedinstallation.buildtype

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.appbuildconfig.api.BuildFlavor
import com.duckduckgo.appbuildconfig.api.BuildFlavor.FDROID
import com.duckduckgo.appbuildconfig.api.BuildFlavor.PLAY
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class VerificationCheckBuildTypeImplTest {

    private val appBuildConfig: AppBuildConfig = mock()
    private val testee = VerificationCheckBuildTypeImpl(appBuildConfig)

    @Test
    fun whenIsPlayReleaseThenIdentifiedCorrectly() {
        configureFlavor(PLAY)
        configureAsReleaseBuild()
        assertTrue(testee.isPlayReleaseBuild())
    }

    @Test
    fun whenIsPlayAndNotReleaseThenIdentifiedCorrectly() {
        configureFlavor(PLAY)
        configureAsDebugBuild()
        assertFalse(testee.isPlayReleaseBuild())
    }

    @Test
    fun whenIsNotPlayFlavorThenIdentifiedCorrectly() {
        configureFlavor(FDROID)
        configureAsReleaseBuild()
        assertFalse(testee.isPlayReleaseBuild())
    }

    @Test
    fun whenIsNotPlayFlavorAndNotReleaseThenIdentifiedCorrectly() {
        configureFlavor(FDROID)
        configureAsDebugBuild()
        assertFalse(testee.isPlayReleaseBuild())
    }

    private fun configureAsReleaseBuild() {
        whenever(appBuildConfig.isDebug).thenReturn(false)
    }

    private fun configureAsDebugBuild() {
        whenever(appBuildConfig.isDebug).thenReturn(true)
    }

    private fun configureFlavor(flavor: BuildFlavor) {
        whenever(appBuildConfig.flavor).thenReturn(flavor)
    }
}
