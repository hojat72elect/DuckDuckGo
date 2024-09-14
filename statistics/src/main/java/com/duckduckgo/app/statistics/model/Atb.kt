

package com.duckduckgo.app.statistics.model

data class Atb(
    val version: String,
    val updateVersion: String? = null,
) {

    fun formatWithVariant(variantKey: String?): String {
        return version + variantKey.orEmpty()
    }
}
