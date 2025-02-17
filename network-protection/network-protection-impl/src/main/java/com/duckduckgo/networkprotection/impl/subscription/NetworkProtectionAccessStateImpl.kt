package com.duckduckgo.networkprotection.impl.subscription

import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.navigation.api.GlobalActivityStarter.ActivityParams
import com.duckduckgo.networkprotection.api.NetworkProtectionAccessState
import com.duckduckgo.networkprotection.api.NetworkProtectionAccessState.NetPAccessState
import com.duckduckgo.networkprotection.api.NetworkProtectionAccessState.NetPAccessState.Locked
import com.duckduckgo.networkprotection.api.NetworkProtectionAccessState.NetPAccessState.UnLocked
import com.duckduckgo.networkprotection.api.NetworkProtectionScreens.NetworkProtectionManagementScreenAndEnable
import com.duckduckgo.networkprotection.api.NetworkProtectionScreens.NetworkProtectionManagementScreenNoParams
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@ContributesBinding(AppScope::class)
class NetworkProtectionAccessStateImpl @Inject constructor(
    private val networkProtectionState: NetworkProtectionState,
    private val dispatcherProvider: DispatcherProvider,
    private val netpSubscriptionManager: NetpSubscriptionManager,
) : NetworkProtectionAccessState {

    override suspend fun getState(): NetPAccessState = withContext(dispatcherProvider.io()) {
        return@withContext if (!netpSubscriptionManager.getVpnStatus().isActive()) {
            // if entitlement check succeeded and no entitlement, reset state and hide access.
            handleRevokedVPNState()
            Locked
        } else {
            UnLocked
        }
    }

    override suspend fun getStateFlow(): Flow<NetPAccessState> =
        withContext(dispatcherProvider.io()) {
            netpSubscriptionManager.vpnStatus().map { status ->
                if (!status.isActive()) {
                    // if entitlement check succeeded and no entitlement, reset state and hide access.
                    handleRevokedVPNState()
                    Locked
                } else {
                    UnLocked
                }
            }
        }

    private suspend fun handleRevokedVPNState() {
        if (networkProtectionState.isEnabled()) {
            networkProtectionState.stop()
        }
    }

    override suspend fun getScreenForCurrentState(): ActivityParams? {
        return when (getState()) {
            is UnLocked -> {
                if (networkProtectionState.isOnboarded()) {
                    NetworkProtectionManagementScreenNoParams
                } else {
                    NetworkProtectionManagementScreenAndEnable(false)
                }
            }

            Locked -> null
        }
    }
}
