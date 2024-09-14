

package com.duckduckgo.autofill.sync

import com.duckduckgo.sync.api.SyncCrypto

class FakeCrypto : SyncCrypto {
    override fun encrypt(text: String) = text

    override fun decrypt(data: String) = data
}
