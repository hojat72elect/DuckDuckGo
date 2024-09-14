

package com.duckduckgo.networkprotection.api

import kotlinx.coroutines.flow.Flow

interface NetworkProtectionState {
    /**
     * This is a suspend function because the operation has I/O.
     * You DO NOT need to set any dispatcher to call this suspend function
     * @return `true` when NetP was onboarded (enabled at least once in the past), `false` otherwise
     */
    suspend fun isOnboarded(): Boolean

    /**
     * This is a suspend function because the operation can take time.
     * You DO NOT need to set any dispatcher to call this suspend function
     * @return `true` when NetP is enabled
     */
    suspend fun isEnabled(): Boolean

    /**
     * This is a suspend function because the operation can take time
     * You DO NOT need to set any dispatcher to call this suspend function
     * @return `true` when NetP is enabled AND the VPN is running, `false` otherwise
     */
    suspend fun isRunning(): Boolean

    /**
     * This method will start the Network Protection feature
     */
    fun start()

    /**
     * This method will restart the Network Protection feature by disabling it and re-enabling back again
     */
    fun restart()

    /**
     * This method is the same as [restart] but it also clears the existing VPN reconfiguration, forcing a new registration
     * process with the VPN backend
     */
    fun clearVPNConfigurationAndRestart()

    /**
     * This method will stop the Network Protection feature by disabling it
     */
    suspend fun stop()

    /**
     * This method is the same as [stop] but it also clears the existing VPN reconfiguration, forcing a new registration
     * process with the VPN backend
     */
    fun clearVPNConfigurationAndStop()

    /**
     * This method returns the current server location Network Protection is routing device's data through.
     * @return Returns the server location if available, otherwise null.
     */
    fun serverLocation(): String?

    /**
     * This method returns the [ConnectionState] of Network Protection which considers both the feature state and the VPNService state.
     * @return [ConnectionState] of Network Protection.
     */
    fun getConnectionStateFlow(): Flow<ConnectionState>

    enum class ConnectionState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
    }
}
