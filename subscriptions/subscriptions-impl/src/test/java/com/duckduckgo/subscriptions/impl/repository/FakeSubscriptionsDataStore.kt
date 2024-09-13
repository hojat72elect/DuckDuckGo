package com.duckduckgo.subscriptions.impl.repository

import com.duckduckgo.subscriptions.impl.store.SubscriptionsDataStore

class FakeSubscriptionsDataStore(private val supportEncryption: Boolean = true) :
    SubscriptionsDataStore {

    // Auth
    override var accessToken: String? = null
    override var authToken: String? = null
    override var email: String? = null
    override var externalId: String? = null

    // Subscription
    override var expiresOrRenewsAt: Long? = 0L
    override var platform: String? = null
    override var startedAt: Long? = 0L
    override var status: String? = null
    override var entitlements: String? = null
    override var productId: String? = null
    override fun canUseEncryption(): Boolean = supportEncryption
}
