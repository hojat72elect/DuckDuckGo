

package com.duckduckgo.networkprotection.impl.config

import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.VpnScope
import com.duckduckgo.networkprotection.impl.exclusion.systemapps.SystemAppsExclusionRepository
import com.duckduckgo.networkprotection.impl.settings.NetPSettingsLocalConfig
import com.duckduckgo.networkprotection.impl.settings.NetpVpnSettingsDataStore
import com.duckduckgo.networkprotection.store.NetPExclusionListRepository
import com.squareup.anvil.annotations.ContributesBinding
import java.net.Inet4Address
import java.net.InetAddress
import javax.inject.Inject
import kotlinx.coroutines.withContext

interface NetPDefaultConfigProvider {
    fun mtu(): Int = 1280

    suspend fun exclusionList(): Set<String> = emptySet()

    fun fallbackDns(): Set<InetAddress> = emptySet()

    suspend fun routes(): Map<String, Int> = emptyMap()

    fun pcapConfig(): PcapConfig? = null
}

data class PcapConfig(
    val filename: String,
    val snapLen: Int,
    val fileSize: Int,
)

@ContributesBinding(VpnScope::class)
class RealNetPDefaultConfigProvider @Inject constructor(
    private val netPExclusionListRepository: NetPExclusionListRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val netPSettingsLocalConfig: NetPSettingsLocalConfig,
    private val systemAppsExclusionRepository: SystemAppsExclusionRepository,
    private val netpVpnSettingsDataStore: NetpVpnSettingsDataStore,
) : NetPDefaultConfigProvider {
    override suspend fun exclusionList(): Set<String> {
        return mutableSetOf<String>().apply {
            addAll(netPExclusionListRepository.getExcludedAppPackages())
            addAll(systemAppsExclusionRepository.getAllExcludedSystemApps())
        }.toSet()
    }

    override suspend fun routes(): Map<String, Int> = withContext(dispatcherProvider.io()) {
        return@withContext if (netPSettingsLocalConfig.vpnExcludeLocalNetworkRoutes().isEnabled()) {
            WgVpnRoutes.wgVpnDefaultRoutes.toMutableMap().apply {
                fallbackDns().filterIsInstance<Inet4Address>().mapNotNull { it.hostAddress }.forEach { ip ->
                    this[ip] = 32
                }
            }
        } else {
            WgVpnRoutes.wgVpnRoutesIncludingLocal.toMutableMap().apply {
                fallbackDns().filterIsInstance<Inet4Address>().mapNotNull { it.hostAddress }.forEach { ip ->
                    this[ip] = 32
                }
            }
        }
    }

    override fun fallbackDns(): Set<InetAddress> {
        return netpVpnSettingsDataStore.customDns?.run {
            runCatching {
                InetAddress.getAllByName(this).toSet()
            }.getOrDefault(emptySet())
        } ?: emptySet()
    }
}
