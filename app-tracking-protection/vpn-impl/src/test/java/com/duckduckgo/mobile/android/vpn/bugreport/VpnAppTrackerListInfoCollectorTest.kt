

package com.duckduckgo.mobile.android.vpn.bugreport

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.mobile.android.vpn.dao.VpnAppTrackerBlockingDao
import com.duckduckgo.mobile.android.vpn.store.VpnDatabase
import com.duckduckgo.mobile.android.vpn.trackers.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class VpnAppTrackerListInfoCollectorTest {

    private val vpnDatabase = mock<VpnDatabase>()
    private val vpnAppTrackerBlockingDao = mock<VpnAppTrackerBlockingDao>()

    private val appTrackerRepository = mock<AppTrackerRepository>()

    private lateinit var collector: VpnAppTrackerListInfoCollector

    @Before
    fun setup() {
        whenever(vpnDatabase.vpnAppTrackerBlockingDao()).thenReturn(vpnAppTrackerBlockingDao)
        whenever(appTrackerRepository.getManualAppExclusionList()).thenReturn(listOf())

        collector = VpnAppTrackerListInfoCollector(vpnDatabase, appTrackerRepository)
    }

    @Test
    fun whenCollectStateAndBlocklistEtagNotPesentThenJsonEtagIsEmpty() = runTest {
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("", jsonObject.get("appTrackerListEtag"))
    }

    @Test
    fun whenCollectStateAndBlocklistEtagPesentThenJsonEtagHasValue() = runTest {
        whenever(vpnAppTrackerBlockingDao.getTrackerBlocklistMetadata()).thenReturn(AppTrackerMetadata(0, "etag"))
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("etag", jsonObject.get("appTrackerListEtag"))
    }

    @Test
    fun whenCollectStateAndExclusionListEtagNotPresentThenJsonEtagIsEmpty() = runTest {
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("", jsonObject.get("appExclusionListEtag"))
    }

    @Test
    fun whenCollectStateAndExclusionListEtagPresentThenJsonEtagHasValue() = runTest {
        whenever(vpnAppTrackerBlockingDao.getExclusionListMetadata()).thenReturn(AppTrackerExclusionListMetadata(0, "etag"))
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("etag", jsonObject.get("appExclusionListEtag"))
    }

    @Test
    fun whenCollectStateAndExclusionRuleListEtagNotPresentThenJsonEtagIsEmpty() = runTest {
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("", jsonObject.get("appExceptionRuleListEtag"))
    }

    @Test
    fun whenCollectStateAndExclusionRuleListEtagPresentThenJsonEtagHasValue() = runTest {
        whenever(vpnAppTrackerBlockingDao.getTrackerExceptionRulesMetadata()).thenReturn(AppTrackerExceptionRuleMetadata(0, "etag"))
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("etag", jsonObject.get("appExceptionRuleListEtag"))
    }

    @Test
    fun whenCollectStateAndAppNotGameOrInExclusionListThenReturnUnprotectedByDefaultFalse() = runTest {
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("false", jsonObject.get("reportedAppUnprotectedByDefault"))
    }

    @Test
    fun whenCollectStateAndProtectionOverridenThenOverridenDefaultProtectionTrue() = runTest {
        whenever(appTrackerRepository.getManualAppExclusionList()).thenReturn(listOf((AppTrackerManualExcludedApp(PACKAGE_ID, true))))
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("true", jsonObject.get("overridenDefaultProtection"))
    }

    @Test
    fun whenCollectStateAndProtectionOverridenThenOverridenDefaultProtectionFalse() = runTest {
        whenever(appTrackerRepository.getManualAppExclusionList()).thenReturn(listOf((AppTrackerManualExcludedApp("other.package.id", true))))
        val jsonObject = collector.collectVpnRelatedState(PACKAGE_ID)

        assertEquals("false", jsonObject.get("overridenDefaultProtection"))
    }

    companion object {
        private const val PACKAGE_ID = "com.package.id"
        private const val REASON = "UNKNOWN"
    }
}
