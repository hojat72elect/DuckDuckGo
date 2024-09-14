

package com.duckduckgo.subscriptions.api

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface Subscriptions {

    /**
     * This method returns a [String] with the access token for the authenticated user or [null] if it doesn't exist
     * or any errors arise.
     * @return [String]
     */
    suspend fun getAccessToken(): String?

    /**
     * This method returns a [true] if a  given [product] can be found in the entitlements list or [false] otherwise
     * @return [Boolean]
     */
    fun getEntitlementStatus(): Flow<List<Product>>

    /**
     * @return `true` if the Privacy Pro product is available for the user, `false` otherwise
     */
    suspend fun isEligible(): Boolean

    /**
     * @return `SubscriptionStatus` with the current subscription status
     */
    suspend fun getSubscriptionStatus(): SubscriptionStatus

    /**
     * @return `true` if the given URL can be handled internally or `false` otherwise
     */
    fun shouldLaunchPrivacyProForUrl(url: String): Boolean

    /**
     * Launches Privacy Pro with Settings as the parent activity
     */
    fun launchPrivacyPro(context: Context, uri: Uri?)
}

enum class Product(val value: String) {
    NetP("Network Protection"),
    ITR("Identity Theft Restoration"),
    PIR("Data Broker Protection"),
}

enum class SubscriptionStatus(val statusName: String) {
    AUTO_RENEWABLE("Auto-Renewable"),
    NOT_AUTO_RENEWABLE("Not Auto-Renewable"),
    GRACE_PERIOD("Grace Period"),
    INACTIVE("Inactive"),
    EXPIRED("Expired"),
    UNKNOWN("Unknown"),
    WAITING("Waiting"),
}
