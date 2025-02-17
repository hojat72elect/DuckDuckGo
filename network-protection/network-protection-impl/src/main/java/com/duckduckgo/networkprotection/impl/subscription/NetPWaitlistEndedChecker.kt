package com.duckduckgo.networkprotection.impl.subscription

import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.VpnScope
import com.duckduckgo.mobile.android.vpn.service.VpnServiceCallbacks
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor.VpnStopReason
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.duckduckgo.networkprotection.impl.pixels.NetworkProtectionPixels
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logcat.logcat

@ContributesMultibinding(VpnScope::class)
@SingleInstanceIn(VpnScope::class)
class NetPWaitlistEndedChecker @Inject constructor(
    private val netpSubscriptionManager: NetpSubscriptionManager,
    private val dispatcherProvider: DispatcherProvider,
    private val networkProtectionState: NetworkProtectionState,
    private val sharedPreferencesProvider: com.duckduckgo.data.store.api.SharedPreferencesProvider,
    private val networkProtectionPixels: NetworkProtectionPixels,
) : VpnServiceCallbacks {

    private val preferences: SharedPreferences by lazy {
        sharedPreferencesProvider.getSharedPreferences("com.duckduckgo.networkprotection.subscription.vpnWaitlistChecker")
    }

    override fun onVpnStarted(coroutineScope: CoroutineScope) {
        coroutineScope.launch(dispatcherProvider.io()) {
            if (!preferences.isAlreadyChecked() && networkProtectionState.isEnabled()) {
                preferences.checkDone()
                var hasEntitlement = false
                // I know, I don't like it either, but it seems we can't ensure a race otherwise
                for (retries in 1..5) {
                    hasEntitlement = netpSubscriptionManager.getVpnStatus().isActive()
                    if (hasEntitlement) return@launch
                    delay(200)
                }
                if (!hasEntitlement) {
                    logcat { "VPN enabled and privacy pro enabled but no entitlements, stopping VPN..." }
                    networkProtectionPixels.reportVpnBetaStoppedWhenPrivacyProUpdatedAndEnabled()
                    networkProtectionState.stop()
                }
            }
        }
    }

    private fun SharedPreferences.checkDone() {
        this.edit(commit = true) { putBoolean("done", true) }
    }

    private fun SharedPreferences.isAlreadyChecked(): Boolean {
        return this.getBoolean("done", false)
    }

    override fun onVpnStopped(
        coroutineScope: CoroutineScope,
        vpnStopReason: VpnStopReason,
    ) {
    }
}
