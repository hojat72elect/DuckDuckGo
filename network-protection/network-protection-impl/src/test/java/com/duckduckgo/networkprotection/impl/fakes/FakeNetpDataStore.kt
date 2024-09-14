

package com.duckduckgo.networkprotection.impl.fakes

import com.duckduckgo.networkprotection.store.NetpDataStore

class FakeNetpDataStore(
    override var authToken: String? = null,
    override var didAcceptedTerms: Boolean = false,
) : NetpDataStore {
    override fun clear() {
        authToken = null
        didAcceptedTerms = false
    }
}
