

package com.duckduckgo.autofill.impl.encoding

class TestUrlUnicodeNormalizer : UrlUnicodeNormalizer {

    var overrides = mutableMapOf<String, String>()

    override fun normalizeAscii(url: String?): String? {
        overrides[url]?.let {
            return it
        }
        return url
    }

    override fun normalizeUnicode(url: String?): String? {
        overrides[url]?.let {
            return it
        }
        return url
    }
}
