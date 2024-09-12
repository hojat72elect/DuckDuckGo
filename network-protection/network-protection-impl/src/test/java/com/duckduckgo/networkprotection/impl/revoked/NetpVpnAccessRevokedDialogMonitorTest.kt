package com.duckduckgo.networkprotection.impl.revoked

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.duckduckgo.networkprotection.impl.subscription.NetpSubscriptionManager
import com.duckduckgo.networkprotection.impl.subscription.NetpSubscriptionManager.VpnStatus.ACTIVE
import com.duckduckgo.networkprotection.impl.subscription.NetpSubscriptionManager.VpnStatus.EXPIRED
import com.duckduckgo.networkprotection.impl.subscription.NetpSubscriptionManager.VpnStatus.INACTIVE
import com.duckduckgo.networkprotection.impl.subscription.NetpSubscriptionManager.VpnStatus.SIGNED_OUT
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NetpVpnAccessRevokedDialogMonitorTest {
    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Mock
    private lateinit var netpSubscriptionManager: NetpSubscriptionManager

    @Mock
    private lateinit var accessRevokedDialog: AccessRevokedDialog

    @Mock
    lateinit var networkProtectionState: NetworkProtectionState

    private lateinit var netpVpnAccessRevokedDialogMonitor: NetpVpnAccessRevokedDialogMonitor

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        runBlocking { whenever(networkProtectionState.isOnboarded()) }.thenReturn(true)
        runBlocking { whenever(networkProtectionState.isEnabled()) }.thenReturn(false)

        netpVpnAccessRevokedDialogMonitor = NetpVpnAccessRevokedDialogMonitor(
            netpSubscriptionManager,
            coroutineTestRule.testScope,
            coroutineTestRule.testDispatcherProvider,
            accessRevokedDialog,
            networkProtectionState,
        )
    }

    @Test
    fun whenVPNStateIsInactiveThenDontShowDialogs() {
        coroutineTestRule.testScope.launch {
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(INACTIVE)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(networkProtectionState, never()).stop()
            verify(accessRevokedDialog).clearIsShown()
        }
    }

    @Test
    fun whenVPNStateIsActiveThenDontShowDialogs() {
        coroutineTestRule.testScope.launch {
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(ACTIVE)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(networkProtectionState, never()).stop()
            verify(accessRevokedDialog).clearIsShown()
        }
    }

    @Test
    fun whenVPNEnabledAndStateIsActiveThenDontShowDialogs() {
        coroutineTestRule.testScope.launch {
            whenever(networkProtectionState.isEnabled()).thenReturn(true)
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(ACTIVE)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(networkProtectionState, never()).stop()
            verify(accessRevokedDialog).clearIsShown()
        }
    }

    @Test
    fun whenVPNEnabledAndStateIsInactiveThenDontShowDialogs() {
        coroutineTestRule.testScope.launch {
            whenever(networkProtectionState.isEnabled()).thenReturn(true)
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(INACTIVE)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(networkProtectionState, never()).stop()
            verify(accessRevokedDialog).clearIsShown()
        }
    }

    @Test
    fun whenVPNStateIsSignedOutThenDontShowDialogs() {
        coroutineTestRule.testScope.launch {
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(SIGNED_OUT)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(networkProtectionState, never()).stop()
            verify(accessRevokedDialog).clearIsShown()
        }
    }

    @Test
    fun whenVPNEnabledAndStateIsSignedOutThenDontShowDialogs() {
        coroutineTestRule.testScope.launch {
            whenever(networkProtectionState.isEnabled()).thenReturn(true)
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(SIGNED_OUT)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(networkProtectionState).stop()
            verify(accessRevokedDialog).clearIsShown()
        }
    }

    @Test
    fun whenVPNStateIsExpiredThenShowAccessRevokedDialog() {
        coroutineTestRule.testScope.launch {
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(EXPIRED)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(accessRevokedDialog).showOnce(any())
            verify(networkProtectionState, never()).stop()
        }
    }

    @Test
    fun whenVPNEnabledStateIsExpiredThenShowAccessRevokedDialog() {
        coroutineTestRule.testScope.launch {
            whenever(networkProtectionState.isEnabled()).thenReturn(true)
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(EXPIRED)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(accessRevokedDialog).showOnce(any())
            verify(networkProtectionState).stop()
        }
    }

    @Test
    fun whenVPNNotOnboardedAndStateIsExpiredThenDontShowDialogs() {
        coroutineTestRule.testScope.launch {
            whenever(networkProtectionState.isOnboarded()).thenReturn(false)
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(EXPIRED)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(accessRevokedDialog, never()).showOnce(any())
            verify(networkProtectionState, never()).stop()
            verify(accessRevokedDialog.clearIsShown())
        }
    }

    @Test
    fun whenVPNEnabledNotOnboardedAndStateIsExpiredThenDontShowDialogs() {
        coroutineTestRule.testScope.launch {
            whenever(networkProtectionState.isOnboarded()).thenReturn(false)
            whenever(networkProtectionState.isEnabled()).thenReturn(true)
            whenever(netpSubscriptionManager.getVpnStatus()).thenReturn(EXPIRED)

            netpVpnAccessRevokedDialogMonitor.onActivityResumed(mock())

            verify(accessRevokedDialog, never()).showOnce(any())
            verify(networkProtectionState, never()).stop()
            verify(accessRevokedDialog.clearIsShown())
        }
    }
}
