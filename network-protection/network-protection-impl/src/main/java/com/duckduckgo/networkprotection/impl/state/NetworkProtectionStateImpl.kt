

package com.duckduckgo.networkprotection.impl.state

import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.VpnFeaturesRegistry
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor.VpnRunningState.ENABLED
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor.VpnRunningState.ENABLING
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.duckduckgo.networkprotection.api.NetworkProtectionState.ConnectionState
import com.duckduckgo.networkprotection.api.NetworkProtectionState.ConnectionState.CONNECTED
import com.duckduckgo.networkprotection.api.NetworkProtectionState.ConnectionState.CONNECTING
import com.duckduckgo.networkprotection.api.NetworkProtectionState.ConnectionState.DISCONNECTED
import com.duckduckgo.networkprotection.impl.NetPVpnFeature
import com.duckduckgo.networkprotection.impl.cohort.NetpCohortStore
import com.duckduckgo.networkprotection.impl.configuration.WgTunnelConfig
import com.duckduckgo.networkprotection.impl.configuration.asServerDetails
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@ContributesBinding(AppScope::class)
class NetworkProtectionStateImpl @Inject constructor(
    private val vpnFeaturesRegistry: VpnFeaturesRegistry,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
    private val cohortStore: NetpCohortStore,
    private val dispatcherProvider: DispatcherProvider,
    private val wgTunnelConfig: WgTunnelConfig,
    private val vpnStateMonitor: VpnStateMonitor,
) : NetworkProtectionState {
    override suspend fun isOnboarded(): Boolean = withContext(dispatcherProvider.io()) {
        return@withContext cohortStore.cohortLocalDate != null
    }

    override suspend fun isEnabled(): Boolean {
        return vpnFeaturesRegistry.isFeatureRegistered(NetPVpnFeature.NETP_VPN)
    }

    override suspend fun isRunning(): Boolean {
        return vpnFeaturesRegistry.isFeatureRunning(NetPVpnFeature.NETP_VPN)
    }

    override fun start() {
        coroutineScope.launch(dispatcherProvider.io()) {
            vpnFeaturesRegistry.registerFeature(NetPVpnFeature.NETP_VPN)
        }
    }

    override fun restart() {
        coroutineScope.launch(dispatcherProvider.io()) {
            vpnFeaturesRegistry.refreshFeature(NetPVpnFeature.NETP_VPN)
        }
    }

    override fun clearVPNConfigurationAndRestart() {
        coroutineScope.launch(dispatcherProvider.io()) {
            wgTunnelConfig.clearWgConfig()
            restart()
        }
    }

    override suspend fun stop() {
        vpnFeaturesRegistry.unregisterFeature(NetPVpnFeature.NETP_VPN)
    }

    override fun clearVPNConfigurationAndStop() {
        coroutineScope.launch(dispatcherProvider.io()) {
            wgTunnelConfig.clearWgConfig()
            stop()
        }
    }

    override fun serverLocation(): String? {
        return runBlocking { wgTunnelConfig.getWgConfig() }?.asServerDetails()?.location
    }

    override fun getConnectionStateFlow(): Flow<ConnectionState> {
        return vpnStateMonitor.getStateFlow(NetPVpnFeature.NETP_VPN).map {
            when (it.state) {
                ENABLED -> CONNECTED
                ENABLING -> CONNECTING
                else -> DISCONNECTED
            }
        }
    }
}
