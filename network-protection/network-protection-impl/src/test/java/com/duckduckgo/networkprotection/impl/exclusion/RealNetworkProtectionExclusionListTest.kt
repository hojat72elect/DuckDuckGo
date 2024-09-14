

package com.duckduckgo.networkprotection.impl.exclusion

import com.duckduckgo.mobile.android.vpn.VpnFeaturesRegistry
import com.duckduckgo.networkprotection.impl.NetPVpnFeature
import com.duckduckgo.networkprotection.store.NetPExclusionListRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class RealNetworkProtectionExclusionListTest {

    @Mock
    private lateinit var vpnFeaturesRegistry: VpnFeaturesRegistry

    @Mock
    private lateinit var netPExclusionListRepository: NetPExclusionListRepository

    private lateinit var testee: RealNetworkProtectionExclusionList

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testee = RealNetworkProtectionExclusionList(netPExclusionListRepository, vpnFeaturesRegistry)
    }

    @Test
    fun whenNetpIsEnabledAndAppIsInExcludedPackagesThenReturnIsExcludedTrue() = runTest {
        whenever(netPExclusionListRepository.getExcludedAppPackages()).thenReturn(listOf("com.test.app"))
        whenever(vpnFeaturesRegistry.isFeatureRegistered(NetPVpnFeature.NETP_VPN)).thenReturn(true)

        assertTrue(testee.isExcluded("com.test.app"))
    }

    @Test
    fun whenNetpIsDisabledAndAppIsInExcludedPackagesThenReturnIsExcludedFalse() = runTest {
        whenever(netPExclusionListRepository.getExcludedAppPackages()).thenReturn(listOf("com.test.app"))
        whenever(vpnFeaturesRegistry.isFeatureRegistered(NetPVpnFeature.NETP_VPN)).thenReturn(false)

        assertFalse(testee.isExcluded("com.test.app"))
    }

    @Test
    fun whenNetpIsEnabledAndAppIsNotInExcludedPackagesThenReturnIsExcludedFalse() = runTest {
        whenever(netPExclusionListRepository.getExcludedAppPackages()).thenReturn(emptyList())
        whenever(vpnFeaturesRegistry.isFeatureRegistered(NetPVpnFeature.NETP_VPN)).thenReturn(true)

        assertFalse(testee.isExcluded("com.test.app"))
    }

    @Test
    fun whenNetpIsNotEnabledAndAppIsNotInExcludedPackagesThenReturnIsExcludedFalse() = runTest {
        whenever(netPExclusionListRepository.getExcludedAppPackages()).thenReturn(emptyList())
        whenever(vpnFeaturesRegistry.isFeatureRegistered(NetPVpnFeature.NETP_VPN)).thenReturn(false)

        assertFalse(testee.isExcluded("com.test.app"))
    }
}
