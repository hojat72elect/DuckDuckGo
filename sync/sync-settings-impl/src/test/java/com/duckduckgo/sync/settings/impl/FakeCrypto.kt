

package com.duckduckgo.sync.settings.impl

import com.duckduckgo.sync.api.*

class FakeCrypto : SyncCrypto {
    override fun encrypt(text: String) = text

    override fun decrypt(data: String) = data
}
