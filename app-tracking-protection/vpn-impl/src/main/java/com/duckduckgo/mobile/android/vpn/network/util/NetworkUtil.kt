

package com.duckduckgo.mobile.android.vpn.network.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.VpnService
import com.duckduckgo.mobile.android.vpn.service.Route
import java.net.Inet4Address
import java.net.InetAddress

fun Context.getSystemActiveNetworkDefaultDns(): List<String> {
    return runCatching {
        val dnsList = mutableListOf<String>()

        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetwork?.let { activeNetwork ->
            connectivityManager.getLinkProperties(activeNetwork)?.let { linkProperties ->
                linkProperties.dnsServers.forEach { dns ->
                    dns.hostAddress?.let {
                        dnsList.add(it)
                    }
                }
            }
        }

        dnsList.toList()
    }.getOrDefault(emptyList())
}

fun Context.getSystemActiveNetworkSearchDomain(): String? {
    return runCatching {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.activeNetwork?.let { activeNetwork ->
            connectivityManager.getLinkProperties(activeNetwork)?.domains
        }
    }.getOrNull()
}

fun InetAddress.isLocal(): Boolean {
    return isLoopbackAddress
}

fun Inet4Address.asRoute(): Route? {
    return hostAddress?.let {
        Route(
            address = it,
            maskWidth = 32,
            lowAddress = it,
            highAddress = it,
        )
    }
}

fun Context.getActiveNetwork(): Network? {
    return runCatching {
        (getSystemService(VpnService.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetwork
    }.getOrNull()
}

fun Context.getUnderlyingNetworks(): List<Network> {
    return runCatching {
        val networks = mutableListOf<Network>()
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // first give priority to the default network
        getActiveNetwork()?.let { networks.add(it) }

        // the WIFI and CELL
        connectivityManager.allNetworks.forEach { n ->
            val capabilities = connectivityManager.getNetworkCapabilities(n)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                networks.add(n)
            } else if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
                networks.add(n)
            }
        }
        networks
    }.getOrDefault(emptyList())
}
