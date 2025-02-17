package com.duckduckgo.networkprotection.impl.volume

import com.duckduckgo.common.utils.ConflatedJob
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.VpnScope
import com.duckduckgo.mobile.android.vpn.service.VpnServiceCallbacks
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor.VpnStopReason
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.duckduckgo.networkprotection.impl.WgProtocol
import com.duckduckgo.networkprotection.impl.volume.NetpDataVolumeStore.DataVolume
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ContributesMultibinding(VpnScope::class)
class NetpDataVolumeMonitor @Inject constructor(
    private val wgProtocol: WgProtocol,
    private val dispatcherProvider: DispatcherProvider,
    private val networkProtectionState: NetworkProtectionState,
    private val netpDataVolumeStore: NetpDataVolumeStore,
) : VpnServiceCallbacks {

    private val job = ConflatedJob()

    override fun onVpnStarted(coroutineScope: CoroutineScope) {
        job += coroutineScope.launch {
            startDataVolumeMonitoring()
        }
    }

    override fun onVpnReconfigured(coroutineScope: CoroutineScope) {
        job += coroutineScope.launch {
            startDataVolumeMonitoring()
        }
    }

    override fun onVpnStopped(
        coroutineScope: CoroutineScope,
        vpnStopReason: VpnStopReason,
    ) {
        job.cancel()
        netpDataVolumeStore.dataVolume = DataVolume()
    }

    private suspend fun startDataVolumeMonitoring() = withContext(dispatcherProvider.io()) {
        if (networkProtectionState.isEnabled()) {
            while (isActive && networkProtectionState.isEnabled()) {
                wgProtocol.getStatistics().also {
                    netpDataVolumeStore.dataVolume = DataVolume(
                        receivedBytes = it.receivedBytes,
                        transmittedBytes = it.transmittedBytes,
                    )
                }
                delay(1.seconds.inWholeMilliseconds)
            }
        } else {
            netpDataVolumeStore.dataVolume = DataVolume()
        }
    }
}
