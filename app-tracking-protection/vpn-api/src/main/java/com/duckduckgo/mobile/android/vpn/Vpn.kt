

package com.duckduckgo.mobile.android.vpn

/**
 * Public API to handle start/stop type of actions for the device VPN
 */
interface Vpn {
    /**
     * Enables the device VPN by starting the VPN service
     */
    suspend fun start()

    /**
     * Disable the device VPN by stopping the VPN service
     */
    suspend fun stop()

    /**
     * Snoozes the VPN for [triggerAtMillis] milliseconds
     */
    suspend fun snooze(triggerAtMillis: Long)
}
