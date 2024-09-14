

package com.duckduckgo.sync.impl

import android.util.Base64

internal fun String.encodeB64(): String {
    return Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)
}

internal fun String.decodeB64(): String {
    return String(Base64.decode(this, Base64.DEFAULT))
}
