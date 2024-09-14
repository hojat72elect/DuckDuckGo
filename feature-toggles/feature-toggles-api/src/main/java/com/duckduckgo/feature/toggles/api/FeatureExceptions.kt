
package com.duckduckgo.feature.toggles.api

object FeatureExceptions {
    interface Store {
        fun insertAll(exception: List<FeatureException>)
    }

    data class FeatureException(val domain: String, val reason: String?)

    val EMPTY_STORE = object : Store {
        override fun insertAll(exception: List<FeatureException>) {}
    }
}
