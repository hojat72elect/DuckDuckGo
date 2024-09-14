
package com.duckduckgo.feature.toggles.api

object FeatureSettings {
    interface Store {
        fun store(
            jsonString: String,
        )
    }

    val EMPTY_STORE = object : Store {
        override fun store(jsonString: String) {}
    }
}
