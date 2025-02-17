

package com.duckduckgo.remote.messaging.impl.matchers

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.appbuildconfig.api.BuildFlavor
import com.duckduckgo.appbuildconfig.api.BuildFlavor.INTERNAL
import com.duckduckgo.browser.api.AppProperties
import com.duckduckgo.remote.messaging.impl.models.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AndroidAppAttributeMatcherTest {

    private val appProperties: AppProperties = mock()
    private val appBuildConfig: AppBuildConfig = mock()

    private val testee = AndroidAppAttributeMatcher(appProperties, appBuildConfig)

    @Test
    fun whenFlavorMatchesThenReturnMatch() = runTest {
        givenDeviceProperties(flavor = INTERNAL)

        val result = testee.evaluate(
            Flavor(value = listOf("internal")),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenFlavorDoesNotMatchThenReturnFail() = runTest {
        givenDeviceProperties(flavor = INTERNAL)

        val result = testee.evaluate(
            Flavor(value = listOf("play")),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenAppIdMatchesThenReturnMatch() = runTest {
        givenDeviceProperties(appId = "com.duckduckgo.mobile.android")

        val result = testee.evaluate(
            AppId(value = "com.duckduckgo.mobile.android"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenAppIdDoesNotMatchThenReturnFail() = runTest {
        givenDeviceProperties(appId = "com.duckduckgo.mobile.android")

        val result = testee.evaluate(
            AppId(value = "com.duckduckgo.mobile.android.debug"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenAppVersionEqualOrLowerThanMaxThenReturnMatch() = runTest {
        givenDeviceProperties(appVersion = "5.100.0")

        val result = testee.evaluate(
            AppVersion(max = "5.100.0"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenAppVersionGreaterThanMaxThenReturnFail() = runTest {
        givenDeviceProperties(appVersion = "5.100.0")

        val result = testee.evaluate(
            AppVersion(max = "5.99.0"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenAppVersionEqualOrGreaterThanMinThenReturnMatch() = runTest {
        givenDeviceProperties(appVersion = "5.100.0")

        val result = testee.evaluate(
            AppVersion(min = "5.100.0"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenAppVersionLowerThanMinThenReturnFail() = runTest {
        givenDeviceProperties(appVersion = "5.99.0")

        val result = testee.evaluate(
            AppVersion(min = "5.100.0"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenAppVersionInRangeThenReturnMatch() = runTest {
        givenDeviceProperties(appVersion = "5.150.0")

        val result = testee.evaluate(
            AppVersion(min = "5.99.0", max = "5.200.0"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenAppVersionNotInRangeThenReturnMatch() = runTest {
        givenDeviceProperties(appVersion = "5.000.0")

        val result = testee.evaluate(
            AppVersion(min = "5.100.0", max = "5.200.0"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenAppVersionSameAsDeviceThenReturnMatch() = runTest {
        givenDeviceProperties(appVersion = "5.100.0")

        val result = testee.evaluate(
            AppVersion(value = "5.100.0"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenAppVersionDifferentToDeviceThenReturnFail() = runTest {
        givenDeviceProperties(appVersion = "5.99.0")

        val result = testee.evaluate(
            AppVersion(value = "5.100.0"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenAtbMatchesThenReturnMatch() = runTest {
        givenDeviceProperties(atb = "v105-2")

        val result = testee.evaluate(
            Atb(value = "v105-2"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenAtbDoesNotMatchThenReturnFail() = runTest {
        givenDeviceProperties(atb = "v105-2")

        val result = testee.evaluate(
            Atb(value = "v105-0"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenAppAtbMatchesThenReturnMatch() = runTest {
        givenDeviceProperties(appAtb = "v105-2")

        val result = testee.evaluate(
            AppAtb(value = "v105-2"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenAppAtbDoesNotMatchThenReturnFail() = runTest {
        givenDeviceProperties(appAtb = "v105-2")

        val result = testee.evaluate(
            AppAtb(value = "v105-0"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenSearchAtbMatchesThenReturnMatch() = runTest {
        givenDeviceProperties(searchAtb = "v105-2")

        val result = testee.evaluate(
            SearchAtb(value = "v105-2"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenSearchAtbDoesNotMatchThenReturnFail() = runTest {
        givenDeviceProperties(searchAtb = "v105-2")

        val result = testee.evaluate(
            SearchAtb(value = "v105-0"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenExpVariantMatchesThenReturnMatch() = runTest {
        givenDeviceProperties(expVariant = "zo")

        val result = testee.evaluate(
            ExpVariant(value = "zo"),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenExpVariantDoesNotMatchThenReturnFail() = runTest {
        givenDeviceProperties(expVariant = "zo")

        val result = testee.evaluate(
            ExpVariant(value = "zz"),
        )

        assertEquals(false, result)
    }

    @Test
    fun whenInstalledGPlayMatchesThenReturnMatch() = runTest {
        givenDeviceProperties(installedGPlay = true)

        val result = testee.evaluate(
            InstalledGPlay(value = true),
        )

        assertEquals(true, result)
    }

    @Test
    fun whenInstalledGPlayDoesNotMatchThenReturnFail() = runTest {
        givenDeviceProperties(installedGPlay = false)

        val result = testee.evaluate(
            InstalledGPlay(value = true),
        )

        assertEquals(false, result)
    }

    private fun givenDeviceProperties(
        flavor: BuildFlavor = BuildFlavor.PLAY,
        appId: String = "com.duckduckgo.mobile.android.debug",
        appVersion: String = "5.106.0",
        atb: String = "v105-2",
        appAtb: String = "v105-2",
        searchAtb: String = "v105-2",
        expVariant: String = "zo",
        installedGPlay: Boolean = true,
    ) {
        whenever(appBuildConfig.flavor).thenReturn(flavor)
        whenever(appBuildConfig.applicationId).thenReturn(appId)
        whenever(appBuildConfig.versionName).thenReturn(appVersion)
        whenever(appProperties.atb()).thenReturn(atb)
        whenever(appProperties.appAtb()).thenReturn(appAtb)
        whenever(appProperties.searchAtb()).thenReturn(searchAtb)
        whenever(appProperties.expVariant()).thenReturn(expVariant)
        whenever(appProperties.installedGPlay()).thenReturn(installedGPlay)
    }
}
