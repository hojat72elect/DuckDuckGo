

package com.duckduckgo.networkprotection.impl.state

import com.duckduckgo.anvil.annotations.ContributesPluginPoint
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.VpnFeaturesRegistry
import com.duckduckgo.networkprotection.impl.NetPVpnFeature
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.withContext
import logcat.logcat

interface NetPFeatureRemover {

    /**
     * Call this method to clear all NetP state.
     * DO NOT set any dispatcher for this method, it's done directly from it
     */
    suspend fun removeFeature()

    @ContributesPluginPoint(AppScope::class)
    interface NetPStoreRemovalPlugin {
        /**
         * Any NetP store that keeps some state data about NetP should contribute an implementation of this type
         * so that the entire state can be cleared when necessary
         */
        fun clearStore()
    }
}

@ContributesBinding(AppScope::class)
class NetPFeatureRemoverImpl @Inject constructor(
    private val netpStores: PluginPoint<NetPFeatureRemover.NetPStoreRemovalPlugin>,
    private val dispatcherProvider: DispatcherProvider,
    private val vpnFeaturesRegistry: VpnFeaturesRegistry,
) : NetPFeatureRemover {
    override suspend fun removeFeature() = withContext(dispatcherProvider.io()) {
        netpStores.getPlugins().forEach {
            logcat { "NetP clearing state for ${it.javaClass}" }
            it.clearStore()
        }
        vpnFeaturesRegistry.unregisterFeature(NetPVpnFeature.NETP_VPN)
    }
}
