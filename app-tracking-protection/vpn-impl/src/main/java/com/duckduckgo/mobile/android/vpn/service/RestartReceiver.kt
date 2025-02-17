

package com.duckduckgo.mobile.android.vpn.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.appbuildconfig.api.isInternalBuild
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.common.utils.extensions.registerNotExportedReceiver
import com.duckduckgo.di.scopes.VpnScope
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import logcat.logcat

@ContributesMultibinding(
    scope = VpnScope::class,
    boundType = VpnServiceCallbacks::class,
)
@SingleInstanceIn(VpnScope::class)
class RestartReceiver @Inject constructor(
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
    private val context: Context,
    private val appBuildConfig: AppBuildConfig,
    private val dispatcherProvider: DispatcherProvider,
) : BroadcastReceiver(), VpnServiceCallbacks {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getStringExtra("action")?.lowercase() == "restart") {
            coroutineScope.launch(dispatcherProvider.io()) {
                TrackerBlockingVpnService.restartVpnService(context)
            }
        }
    }

    override fun onVpnStarted(coroutineScope: CoroutineScope) {
        if (appBuildConfig.isInternalBuild()) {
            logcat { "Starting vpn-service receiver" }
            unregister()
            context.registerNotExportedReceiver(this, IntentFilter("vpn-service"))
        }
    }

    override fun onVpnStopped(coroutineScope: CoroutineScope, vpnStopReason: VpnStateMonitor.VpnStopReason) {
        unregister()
    }

    private fun unregister() {
        kotlin.runCatching { context.unregisterReceiver(this) }
    }
}
